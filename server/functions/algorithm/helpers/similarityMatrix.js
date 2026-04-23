const schema = require('../../schemas/answerSchema.js');
// ------------------------------------------------------------
// Hard constraints: if any of these fail → similarity = 0
// Includes:
//   1. Gender preference (A → B and B → A)
//   2. Availability overlap
// ------------------------------------------------------------
function hardConstraintsFail(vecA, vecB) {
    if (!vecA || !vecB) return true;
    // -------------------------
    // Gender preference (A → B)
    // -------------------------
    const pref = schema.fields.genderPreference.indices;
    const gender = schema.fields.gender.indices;


    const A_pref_men = vecA[pref.men] === 1;
    const A_pref_women = vecA[pref.women] === 1;
    const A_pref_none = vecA[pref.none] === 1;

    const B_is_male = vecB[gender.male] === 1;
    const B_is_female = vecB[gender.female] === 1;

    if (!A_pref_none) {
        if (A_pref_men && !B_is_male) return true;
        if (A_pref_women && !B_is_female) return true;
    }

    // -------------------------
    // Gender preference (B → A)
    // -------------------------
    const B_pref_men = vecB[pref.men] === 1;
    const B_pref_women = vecB[pref.women] === 1;
    const B_pref_none = vecB[pref.none] === 1;

    const A_is_male = vecA[gender.male] === 1;
    const A_is_female = vecA[gender.female] === 1;

    if (!B_pref_none) {
        if (B_pref_men && !A_is_male) return true;
        if (B_pref_women && !A_is_female) return true;
    }

    // -------------------------
    // Availability overlap
    // Must share at least one available time slot
    // -------------------------
    const availabilityIdx = schema.fields.availability.indices;

    const overlap = availabilityIdx.some(idx => vecA[idx] === 1 && vecB[idx] === 1);

    if (!overlap) return true;

    return false;
}

// -------------------------
// Jaccard Index for multi-choice fields
// -------------------------
function jaccardIndex(a, b) {
    let intersection = 0;
    let union = 0;

    for (let i = 0; i < a.length; i++) {
        const ai = a[i];
        const bi = b[i];

        if (ai === 1 || bi === 1) union++;
        if (ai === 1 && bi === 1) intersection++;
    }

    if (union === 0) return 0;
    return intersection / union;
}

// -------------------------
// Compute compatibility score between two vectors
// -------------------------
function computeCompatibility(vecA, vecB, weights) {
    // Hard constraints first
    if (hardConstraintsFail(vecA, vecB)) {
        return 0;
    }

    let score = 0;

    for (const [fieldName, field] of Object.entries(schema.fields)) {
        const weight = weights[fieldName];
        // Gender & genderPreference handled in hard constraints
        if (fieldName === "gender" || fieldName === "genderPreference") continue;

        // Multi-choice → Jaccard
        if (field.type === "multi") {
            const a = field.indices.map(i => vecA[i]);
            const b = field.indices.map(i => vecB[i]);
            score += weight * jaccardIndex(a, b);
        }

        // Single-choice → delta
        if (field.type === "single") {
            for (const label of field.labels) {
                const idx = field.indices[label];
                if (vecA[idx] === 1 && vecB[idx] === 1) {
                    score += weight;
                }
            }
        }
    }

    return score;
}

// ------------------------------------------------------------
// Build full similarity matrix (n × n)
// matrix[i][j] = compatibility score
// ------------------------------------------------------------
function buildSimilarityMatrix(vectors, weights) {
    const n = vectors.length;

    // Initialize empty matrix
    const matrix = Array.from({ length: n }, () => Array(n).fill(0));


    for (let i = 0; i < n; i++) {
        for (let j = i + 1; j < n; j++) {

            // Similarity score
            const score = computeCompatibility(vectors[i], vectors[j], weights);

            matrix[i][j] = score;
            matrix[j][i] = score;
        }
    }

    return matrix;
}
module.exports = buildSimilarityMatrix;