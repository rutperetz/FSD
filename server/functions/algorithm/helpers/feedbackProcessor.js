const buildSimilarityMatrix = require("./similarityMatrix");


// -------------------------
// Lock groups based on approvals
// -------------------------
function lockGroups(groups, feedback, minSize) {
    for (const group of groups) {
        if (group.groupStatus) continue;

        const approvers = group.memberIds.filter(s => feedback[s]?.approveGroup);

        function isCompatible(student, current) {
            const sFeedback = feedback[student];

            for (const member of current) {
                const mFeedback = feedback[member];

                if (sFeedback.rejectStudents.includes(member) ||
                    mFeedback.rejectStudents.includes(student)) {
                    return false;
                }
            }
            return true;
        }

        let bestGroup = [];

        function backtrack(start, current) {
            // update bestGroup if current is better
            if (current.length > bestGroup.length) {
                bestGroup = [...current];
            }

            // Pruning: if even taking all remaining approvers won't improve bestGroup, return
            if (current.length + (approvers.length - start) <= bestGroup.length) {
                return;
            }

            for (let i = start; i < approvers.length; i++) {
                const student = approvers[i];

                if (!isCompatible(student, current)) continue;

                current.push(student);
                backtrack(i + 1, current);
                current.pop();
            }
        }

        backtrack(0, []);

        // Lock the group if it meets the minimum size requirement
        if (bestGroup.length >= minSize) {
            group.memberIds = bestGroup;
            group.groupStatus = true;
        }
    }
    // return only locked groups
    groups = groups.filter(g => g.groupStatus);
    return groups;

}

// ------------------------------------------------------------
// Update weights based on feedback (FUTURE FEATURE)
// Currently disabled unless rejectReasons contains real data.
// ------------------------------------------------------------
function updateWeights(weights, feedback, alpha = 0.1) {
    if (!feedback) return { ...weights };

    // Guard: if rejectReasons are empty for all students → do NOT modify weights
    const hasActiveRejectReasons = Object.values(feedback).some(
        f => f.rejectReasons && Object.keys(f.rejectReasons).length > 0
    );

    if (!hasActiveRejectReasons) {
        // Feature intentionally disabled for now
        return { ...weights };
    }

    const newWeights = {};
    const deltas = {};
    const categoryRejections = {};
    const totalFeedback = Object.keys(feedback).length;
    
    // Count rejections per field
    for (const studentKey in feedback) {
        const rejectReasons = feedback[studentKey].rejectReasons;
        for (const field in rejectReasons) {
            categoryRejections[field] = (categoryRejections[field] || 0) + rejectReasons[field];
        }
    }

    // 1. Compute Δ_f for each field
    for (const field in weights) {
        const rejectionCount = categoryRejections[field] || 0;

        // rejectionRate = number of rejections / number of feedbacks
        const rejectionRate = rejectionCount / totalFeedback;

        // Δ_f = α * rejectionRate
        deltas[field] = alpha * rejectionRate;
    }

    // 2. Compute numerator: w_f + Δ_f
    for (const field in weights) {
        newWeights[field] = weights[field] + deltas[field];
    }

    // 3. Normalize: divide by Σ_h (w_h + Δ_h)
    const sum = Object.values(newWeights).reduce((a, b) => a + b, 0);

    for (const field in newWeights) {
        newWeights[field] = newWeights[field] / sum;
    }

    return newWeights;
}

// -------------------------
// Apply rejections global blacklist to similarity matrix
// -------------------------
function applyGlobalBlacklist(matrix, globalBlacklist, idToIndexMap) {
    if (!matrix || !globalBlacklist) return;

    globalBlacklist.forEach(entry => {
        const fromIdx = idToIndexMap[entry.from];
        const toIdx = idToIndexMap[entry.to];

        if (matrix[fromIdx] && matrix[toIdx]) {
            matrix[fromIdx][toIdx] = 0;
            matrix[toIdx][fromIdx] = 0;
        }
    });
}


// ------------------------------------------------------------
// Strengthen similarity between mutually approving members
// ------------------------------------------------------------
function processApprovals(matrix, groups, feedback) {
    for (const group of groups) {
        const approvers = group.memberIds.filter(s => feedback[s]?.approveGroup);
        for (let i = 0; i < approvers.length; i++) {
            for (let j = i + 1; j < approvers.length; j++) {
                const a = approvers[i];
                const b = approvers[j];

                const aFeedback = feedback[a.toString()];
                const bFeedback = feedback[b.toString()];
                // Only if no one rejected the other
                if (!aFeedback.rejectStudents.includes(b) &&
                    !bFeedback.rejectStudents.includes(a)) {
                    matrix[a][b] = 1;
                    matrix[b][a] = 1;
                }
            }
        }
    }
}

// ------------------------------------------------------------
// Main feedback processor
// Rebuilds similarity matrix and applies feedback effects
// ------------------------------------------------------------
function feedbackProcessor(vectors, groups, feedback, weights, globalBlacklist, idToIndexMap) {
    // 1. Update weights
    const updatedWeights = updateWeights(weights, feedback);
    //2. recompute similarity matrix
    const newMatrix = buildSimilarityMatrix(vectors, updatedWeights);
    //3. apply rejections
    applyGlobalBlacklist(newMatrix,globalBlacklist, idToIndexMap);
    //4. apply approvals 
    processApprovals(newMatrix, groups, feedback);

    return { newMatrix, updatedWeights };
}

module.exports = { feedbackProcessor, lockGroups };