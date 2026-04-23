
const { explainGroupReason, computeGroupScore } = require('./helpers/groupReasonAndScore');

// ------------------------------------------------------
// Utility: Mutual preference check
// Ensures every member appears in the windows preferences
//  of every other member
// ------------------------------------------------------
function checkMutualPreference(groupIndices, neighbors, windowSize) {
    for (const i of groupIndices) {
        const prefs = neighbors[i].slice(0, windowSize).map(x => x.idx);

        for (const j of groupIndices) {
            if (i === j) continue;
            if (!prefs.includes(j)) return false;
        }
    }

    return true;
}

// ------------------------------------------------------
// Utility: test whether two indices mutually 
// prefer each other within the current window slice. 
// Used for early pruning during combo generation.
// ------------------------------------------------------
function isMutualPair(i, j, neighbors, windowSize) {
    const prefsI = neighbors[i].slice(0, windowSize).map(x => x.idx);
    if (!prefsI.includes(j)) return false;
    const prefsJ = neighbors[j].slice(0, windowSize).map(x => x.idx);
    return prefsJ.includes(i);
}

// ------------------------------------------------------
// Utility: Generate valid combinations of size k from array arr
//  while pruning on the fly.  
// Any partial combo that violates a "hard" constraint is
// abandoned early, avoiding exponential blow‑up for large groups.
// Hard constraints are:
//   * no zero-similarity pairs (matrix entry == 0)
//   * mutual preference between every pair (using windowSize)
// ------------------------------------------------------
function combinations(arr, k, neighbors, matrix, windowSize) {
    const result = [];
    function helper(start, combo) {
        if (combo.length === k) {
            result.push([...combo]);
            return;
        }
        for (let i = start; i < arr.length; i++) {
            const candidate = arr[i];

            // prune based on relationships between candidate 
            // and existing combo members
            let bad = false;
            for (const existing of combo) {
                if (matrix[existing][candidate] === 0 || matrix[candidate][existing] === 0) {
                    bad = true; // Hard constraint: no zero similarity
                    break;
                }
                if (!isMutualPair(existing, candidate, neighbors, windowSize)) {
                    bad = true;   // Hard constraint: mutual preference within window
                    break;
                }
            }
            if (bad) continue;

            combo.push(candidate);
            helper(i + 1, combo);
            combo.pop();
        }
    }
    helper(0, []);
    return result;
}


// ------------------------------------------------------
// Utility: Dynamic buffer calculation
// Expands the window more for students with low similarity
// to increase their chances of finding a valid group.
// -----------------------------------------------------------------------------------------------------------
function computeDynamicBuffer(maxSize, neighbors, studentIdx, baseBuffer) {
    const top = neighbors[studentIdx].slice(0, maxSize).map(n => n.score);
    const avg = top.reduce((a, b) => a + b, 0) / top.length;

    if (avg < 0.3) return baseBuffer * 2;     // Hard to match
    if (avg < 0.5) return baseBuffer * 1.5;   // Medium difficulty
    return baseBuffer;                        // Normal
}

// ------------------------------------------------------
//Utility: Tie Expansion (with buffer)
// Ensures that if the last neighbor in the window has score X,
// all neighbors with score X (up to buffer limit) are included.
// ------------------------------------------------------
function expandWindowForTies(neighborsList, windowSize, bufferLimit) {
    const base = neighborsList.slice(0, windowSize);
    if (base.length === 0) return [];
    const lastScore = base[base.length - 1].score;

    const tied = neighborsList.filter(n => n.score === lastScore);

    // Combine base + tied, but respect buffer limit
    const combined = [...new Set([...base, ...tied])];
    const limited = combined.slice(0, bufferLimit);

    return limited.map(x => x.idx);
}

// ------------------------------------------------------
// Window-based clustering algorithm with:
//  - dynamic window expansion
//  - tie handling
//  - combination search
//  - best-group selection per anchor
// ------------------------------------------------------
function windowClustering(vectors, matrix, threshold, minSize, maxSize, used) {
    const n = vectors.length;
    const groups = [];
    if (!Array.isArray(vectors) || !Array.isArray(matrix) || n === 0) {
        return { groups: [], used };
    }

    // For each user, get sorted list of neighbors
    const neighbors = new Array(n);
    for (let i = 0; i < n; i++) {
        const row = matrix[i] || [];

        neighbors[i] = row
            .map((score, idx) => ({ idx, score })) 
            .filter(x => x.idx !== i && x.score > 0) 
            .sort((a, b) => b.score - a.score);

    }

    // Base buffer = 10% of class size (minimum 10)
    const baseBuffer = Math.max(10, Math.floor(n * 0.1));

    for (let i = 0; i < n; i++) {
        if (used[i] || neighbors[i].length === 0) continue;

        let groupFormed = false;

        // Maximum window expansion allowed based on student similarity
        const maxWindowLimit = maxSize + computeDynamicBuffer(maxSize, neighbors, i, baseBuffer);

        let windowSize = minSize;

        while (!groupFormed && windowSize <= maxWindowLimit) {
            if (windowSize - 1 > neighbors[i].length) {
                break;
            }
            // Expand window with tie-handling 
            const expandedCandidates = expandWindowForTies(
                neighbors[i],
                windowSize - 1,
                maxWindowLimit
            );
            // Remove already used students
            const candidates = expandedCandidates.filter(idx => !used[idx]);

            if (candidates.length < minSize - 1) {
                windowSize++;
                continue;
            }

            let bestGroup = null;
            let bestScore = -Infinity;

            // Try all group sizes in the allowed range
            for (let size = minSize; size <= maxSize; size++) {

                if (candidates.length < size - 1) continue;

                // Generate all possible combinations of neighbors 
                const combos = combinations(candidates, size - 1, neighbors, matrix, expandedCandidates.length);

                for (const combo of combos) {
                    const groupIndices = [i, ...combo];

                    // Check mutual preference
                    if (!checkMutualPreference(groupIndices, neighbors, expandedCandidates.length)) continue;

                    // Compute group score
                    const score = computeGroupScore(groupIndices, matrix);

                    // Keep the best-scoring valid group
                    if (score >= threshold && score > bestScore) {
                        bestScore = score;
                        bestGroup = groupIndices;
                    }
                }
                // Ensure window grows at least beyond current expanded slice
                windowSize = Math.max(windowSize + 1, expandedCandidates.length + 1);
            }

            // If we found the best group for this window
            if (bestGroup) {
                groups.push({
                    memberIds: bestGroup,
                    groupScore: bestScore,
                    groupStatus: false,
                    groupReasons: explainGroupReason(bestGroup, vectors)
                });

                bestGroup.forEach(idx => used[idx] = true);
                groupFormed = true;
                break;
            }

            windowSize++;
        }
    }

    // Collect unassigned students
    const unassigned = Array.from({ length: used.length }, (_, idx) => idx)
        .filter(idx => !used[idx]);

    const unassignedCopy = [...unassigned];
    // Try to assign unassigned students to existing groups if they meet the threshold
    for (const idx of unassignedCopy) {
        if (neighbors[idx].length === 0) continue; // No neighbors, can't cluster
        let targetGroup = null;
        let targetScore = threshold; // Only consider groups above the threshold
        for (const group of groups) {
            if (group.memberIds.length >= maxSize) continue;  // Skip full groups
            if (group.memberIds.some(memberIdx => matrix[idx][memberIdx] === 0)) continue; // Must have some similarity with all group members
            const score = computeGroupScore([...group.memberIds, idx], matrix);
            if (score >= targetScore) {
                targetScore = score;
                targetGroup = group;
            }
        }

        if (targetGroup && targetGroup.memberIds.length > 0) {
            const removeIndex = unassigned.indexOf(idx);
            if (removeIndex !== -1) {
                unassigned.splice(removeIndex, 1);
            }
            used[idx] = true;
            // Mutate the existing group
            targetGroup.memberIds.push(idx);
            targetGroup.groupScore = targetScore;
            targetGroup.groupReasons = explainGroupReason(targetGroup.memberIds, vectors);
        }
    }

    return { groups, used };
}

module.exports = windowClustering;
