
const schema = require('../../schemas/answerSchema.js');
// ---------------------------
// Group score
// ---------------------------
function computeGroupScore(groupIndices, matrix) {
    let sum = 0;
    let count = 0;

    for (let i = 0; i < groupIndices.length; i++) {
        for (let j = i + 1; j < groupIndices.length; j++) {
            sum += matrix[groupIndices[i]][groupIndices[j]];
            count++;
        }
    }

    return count === 0 ? 0 : sum / count;
}

// ---------------------------
// Group reason explanation
// ---------------------------

function explainGroupReason(groupIndices, vectors) {
    const result = {};

    for (const [fieldName, field] of Object.entries(schema.fields)) {
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