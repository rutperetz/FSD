const schema = require("../../schemas/answerSchema.js");
// -------------------------
// Normalize answers into binary vector
// -------------------------
function normalizeAnswers(ans) {
    const vector = new Array(
        Object.values(schema.indices).flatMap(idx =>
            Array.isArray(idx) ? idx : Object.values(idx)
        ).length
    ).fill(0);

    for (const [fieldName, field] of Object.entries(schema.fields)) {
        const userValue = ans[fieldName];

        if (field.type === "single") {
            for (const label of field.labels) {
                const idx = field.indices[label];
                vector[idx] = userValue === label ? 1 : 0;
            }
        }

        if (field.type === "multi") {
            field.labels.forEach((label, pos) => {
                const idx = field.indices[pos];
                vector[idx] = userValue.includes(label) ? 1 : 0;
            });
        }
    }

    return vector;
}

module.exports = normalizeAnswers;