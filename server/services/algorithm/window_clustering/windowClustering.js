const computeGroupScore = require('../groupScore');
const explainGroupReason = require('../groupReason');

// ------------------------------------------------------
// Mutual preference check
// Ensures every member appears in the top-K preferences of every other member
// ------------------------------------------------------
function checkMutualPreference(groupIndices, neighbors) {
    const K = groupIndices.length + 2;

    for (const i of groupIndices) {
        const prefs = neighbors[i].slice(0, K).map(x => x.idx);

        for (const j of groupIndices) {
            if (i === j) continue;
            if (!prefs.includes(j)) return false;
        }
    }

    return true;
}

// ------------------------------------------------------
// Generate all combinations of size k from array arr
// Used to test all possible group compositions inside the window
// ------------------------------------------------------
function combinations(arr, k) {
    const result = [];
    function helper(start, combo) {
        if (combo.length === k) {
            result.push([...combo]);
            return;
        }
        for (let i = start; i < arr.length; i++) {
            combo.push(arr[i]);
            helper(i + 1, combo);
            combo.pop();
        }
    }
    helper(0, []);
    return result;
}

// ------------------------------------------------------
// Dynamic buffer calculation
// Expands the window more for students with low similarity scores
// ------------------------------------------------------
function computeDynamicBuffer(maxSize, neighbors, studentIdx, baseBuffer) {
    const top = neighbors[studentIdx].slice(0, maxSize).map(n => n.val);
    const avg = top.reduce((a, b) => a + b, 0) / top.length;

    if (avg < 0.3) return baseBuffer * 2;     // Hard to match
    if (avg < 0.5) return baseBuffer * 1.5;   // Medium difficulty
    return baseBuffer;                        // Normal
}

// ------------------------------------------------------
// Tie Expansion (with buffer)
// Ensures that if the last neighbor in the window has score X,
// all neighbors with score X (up to buffer limit) are included.
// ------------------------------------------------------
function expandWindowForTies(neighborsList, windowSize, bufferLimit) {
    // Base window slice
    const base = neighborsList.slice(0, windowSize);

    // Score of the last element in the base window
    const lastScore = base[base.length - 1].val;

    // All neighbors with the same score
    const tied = neighborsList.filter(n => n.val === lastScore);

    // Combine base + tied, but respect buffer limit
    const combined = [...new Set([...base, ...tied])];

    // Limit expansion to windowSize + bufferLimit
    const maxAllowed = windowSize + bufferLimit;
    const limited = combined.slice(0, maxAllowed);

    return limited.map(x => x.idx);
}

// ------------------------------------------------------
// Window Clustering with:
// - Dynamic window
// - Tie expansion
// - Combination search
// - Best-size search
// ------------------------------------------------------
function windowClustering(vectors, matrix, neighbors, minSize, maxSize, threshold) {
    const n = vectors.length;
    const used = new Array(n).fill(false);
    const groups = [];

    // Base buffer = 10% of class size (minimum 2)
    const baseBuffer = Math.max(2, Math.floor(n * 0.1));

    for (let i = 0; i < n; i++) {
        if (used[i]) continue;

        let groupFormed = false;

        // Dynamic buffer based on student similarity
        const dynamicBuffer = computeDynamicBuffer(maxSize, neighbors, i, baseBuffer);

        // Maximum window expansion allowed
        const maxWindowLimit = maxSize + dynamicBuffer;

        let windowSize = minSize;

        while (!groupFormed && windowSize <= maxWindowLimit) {

            // Expand window with tie-handling + buffer
            const expandedCandidates = expandWindowForTies(
                neighbors[i],
                windowSize - 1,
                dynamicBuffer
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
                const combos = combinations(candidates, size - 1);

                for (const combo of combos) {
                    const groupIndices = [i, ...combo];

                    // Check mutual preference
                    if (!checkMutualPreference(groupIndices, neighbors)) continue;

                    // Compute group score
                    const score = computeGroupScore(groupIndices, matrix);

                    // Keep the best-scoring valid group
                    if (score >= threshold && score > bestScore) {
                        bestScore = score;
                        bestGroup = groupIndices;
                    }
                }
            }

            // If we found the best group for this window
            if (bestGroup) {
                groups.push({
                    members: bestGroup,
                    score: bestScore,
                    reasons: explainGroupReason(bestGroup, vectors)
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

    return { groups, unassigned };
}

module.exports = windowClustering;
