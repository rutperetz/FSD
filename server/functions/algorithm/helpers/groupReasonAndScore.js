
const schema = require('../../schemas/answerSchema.js');
// ------------------------------------------------------------
// Compute average pairwise similarity score for a group
// ------------------------------------------------------------
function computeGroupScore(groupIndices, matrix) {
    
    if (!Array.isArray(groupIndices) || !matrix) return 0;

    let sum = 0;
    let count = 0;

    for (let i = 0; i < groupIndices.length; i++) {
        for (let j = i + 1; j < groupIndices.length; j++) {
            const a = groupIndices[i];
            const b = groupIndices[j];

            // Ensure matrix rows exist
            if (!matrix[a] || !matrix[b]) continue;

            sum += matrix[a][b];
            count++;
        }

        return count === 0 ? 0 : sum / count;
    }
}
// ------------------------------------------------------------
// Explain why a group was formed based on shared answers
//  For each field in the schema:
//   - "single": find a label all members selected
//   - "multi": collect all labels shared by all members
// ------------------------------------------------------------

function explainGroupReason(groupIndices, vectors) {
        const result = {};

        if (!Array.isArray(groupIndices) || !vectors) return result;

        for (const [fieldName, field] of Object.entries(schema.fields)) {
            // -----------------------------
            // SINGLE-CHOICE FIELD
            // -----------------------------
            if (field.type === "single") {
                result[fieldName] = null;

                for (const label of field.labels) {
                    const idx = field.indices[label];
                    const allHave = groupIndices.every(i => vectors[i][idx] === 1);

                    if (allHave) {
                        result[fieldName] = label;
                        break;
                    }
                }
            }

            // -----------------------------
            // MULTI-CHOICE FIELD
            // -----------------------------
            if (field.type === "multi") {
                result[fieldName] = [];

                field.labels.forEach((label, pos) => {
                    const idx = field.indices[pos];
                    const allHave = groupIndices.every(i => vectors[i][idx] === 1);

                    if (allHave) {
                        result[fieldName].push(label);
                    }
                });
            }
        }

        return result;
}
    
module.exports = { explainGroupReason, computeGroupScore };