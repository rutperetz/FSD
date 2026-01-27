const buildSimilarityMatrix = require("./similarityMatrix");

// -------------------------
// Update weights based on feedback
// -------------------------
function updateWeights(weights, feedback, alpha = 0.1) {
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
// Apply rejections to similarity matrix
// -------------------------
function updateMatrixWithRejections(matrix, feedback) {
    for (const studentKey in feedback) {
        const studentIndex = parseInt(studentKey);
        const rejected = feedback[studentKey].rejectStudents || [];

        rejected.forEach(otherIndex => {
            matrix[studentIndex][otherIndex] = 0;
            matrix[otherIndex][studentIndex] = 0;
        });
    }
}

// -------------------------
// Lock groups based on approvals
// -------------------------
function lockGroups(groups, feedback, minSize) {
    for (const group of groups) {
       
        const lockedMembers = new Set();
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
                    lockedMembers.add(a);
                    lockedMembers.add(b);
                }
            }
        }
        // If enough locked members, finalize the group
        if (lockedMembers.size >= minSize) {
            group.memberIds = Array.from(lockedMembers);
            group.groupStatus = true;
        }

    }

    return groups;

}
// -------------------------
// Process approvals 
// -------------------------
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
// -------------------------
// made the details for the cluster round
// -------------------------
function feedbackProcessor(vectors,groups, feedback,weights) {
    // 1. Update weights
    const updatedWeights = updateWeights(weights, feedback);  
    //2. recompute similarity matrix
    const newMatrix = buildSimilarityMatrix(vectors, updatedWeights);
    //3. apply rejections
    updateMatrixWithRejections(newMatrix, feedback);
    //4. apply approvals 
    processApprovals(newMatrix, groups, feedback);
    
    return { newMatrix, updatedWeights };
}

module.exports = { feedbackProcessor,lockGroups };

