const schema = require("../../schemas/answerSchema.js");
// ------------------------------------------------------------
// Normalize raw user answers into a binary feature vector.
// Each field in the schema contributes one or more indices:
//   - "single": exactly one index is 1, the rest 0
//   - "multi": multiple indices may be 1
// ------------------------------------------------------------
function normalizeAnswers(ans) {

    if (!ans || typeof ans !== "object") {
        // Return empty vector if answers are missing
        return [];
    }


    // Compute total vector length based on schema indices
    const totalLength = Object.values(schema.indices)
        .flatMap(idx => Array.isArray(idx) ? idx : Object.values(idx))
        .length;

    const vector = new Array(totalLength).fill(0);

    // Iterate through schema fields and encode answers
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