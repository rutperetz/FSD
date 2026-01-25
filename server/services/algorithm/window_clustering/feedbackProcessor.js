const buildSimilarityMatrix = require("./similarityMatrix");

// -------------------------
// Update weights based on feedback
// -------------------------
function updateWeights(weights, feedback, totalStudents, alpha = 0.1) {
    const newWeights = {};
    const deltas = {};
    const categoryRejections = {};
    // Count rejections per field
    for (const cat in feedback.rejectReasons)
    { categoryRejections[cat] = (categoryRejections[cat] || 0) + feedback.rejectReasons[cat]; }
    // 1. Compute Δ_f for each field
    for (const field in weights) {
        const rejectionCount = categoryRejections[field] || 0;

        // rejectionRate = number of rejections / number of students
        const rejectionRate = rejectionCount / totalStudents;

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
// Apply rejections to similarity matrix
// -------------------------
function updateMatrixWithRejections(matrix, feedback) {
    for (const student in feedback) {
        const rejected = feedback[student].rejectStudents || [];
        rejected.forEach(other => {
            matrix[student][other] = 0;
            matrix[other][student] = 0;
        });
    }
}

// -------------------------
// Process approvals and lock groups
// -------------------------
function processApprovalsAndLockGroups(matrix, groups, feedback, minSize, used) {
    for (const group of groups) {
        // If the group is already locked, mark all members as used
        if (group.groupStatus) {
            group.memberIds.forEach(s => used[s] = true);
        } // Already locked
        const lockedMembers = new Set();
        const approvers = group.memberIds.filter(s => feedback[s].approveGroup);
        for (let i = 0; i < approvers.length; i++) {
            for (let j = i + 1; j < approvers.length; j++) {
                const a = approvers[i];
                const b = approvers[j];

                // Only if no one rejected the other
                if (!approvers[a].rejectStudents.includes(b) &&
                    !approvers[b].rejectStudents.includes(a)) {
                    matrix[a][b] = 1;
                    matrix[b][a] = 1;
                    lockedMembers.add(a);
                    lockedMembers.add(b);
                }
            }
        }
        // If enough locked members, finalize the group
        if (lockedMembers.size >= minSize) {
            group.memberIds = Array.from(lockedMembers);
            group.groupStatus = true;
            // Mark locked members as used
            lockedMembers.forEach(s => used[s] = true);
        }
        
    }
    lockedGroups = groups.filter(g => g.groupStatus); // Keep only locked groups
    
    return lockedGroups;
}
// -------------------------
// made the details for the cluster round
// -------------------------
function feedbackProcessor(groups, feedback, minSize, used, totalStudents, weights) {
    // 1. Update weights
    const newWeights = updateWeights(weights, feedback, totalStudents);  
    //2. recompute similarity matrix
    matrix = buildSimilarityMatrix(vectors, newWeights, schema);
    //3. apply rejections
    updateMatrixWithRejections(matrix, feedback);
    //4. apply approvals + lock groups
    const lockedGroups = processApprovalsAndLockGroups(matrix, groups, feedback, minSize, used);
    return { matrix, lockedGroups, newWeights };
}

module.exports = feedbackProcessor ;

