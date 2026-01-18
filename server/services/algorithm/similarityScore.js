function hardConstraintsFail(vecA, vecB, schema) {

    // -------------------------
    // Gender preference (A → B)
    // -------------------------
    const A_pref_men = vecA[schema.indices.genderPref.men] === 1;
    const A_pref_women = vecA[schema.indices.genderPref.women] === 1;
    const A_pref_none = vecA[schema.indices.genderPref.none] === 1;

    const B_is_male = vecB[schema.indices.gender.male] === 1;
    const B_is_female = vecB[schema.indices.gender.female] === 1;

    if (!A_pref_none) {
        if (A_pref_men && !B_is_male) return true;
        if (A_pref_women && !B_is_female) return true;
    }

    // -------------------------
    // Gender preference (B → A)
    // -------------------------
    const B_pref_men = vecB[schema.indices.genderPref.men] === 1;
    const B_pref_women = vecB[schema.indices.genderPref.women] === 1;
    const B_pref_none = vecB[schema.indices.genderPref.none] === 1;

    const A_is_male = vecA[schema.indices.gender.male] === 1;
    const A_is_female = vecA[schema.indices.gender.female] === 1;

    if (!B_pref_none) {
        if (B_pref_men && !A_is_male) return true;
        if (B_pref_women && !A_is_female) return true;
    }

    // -------------------------
    // Availability overlap
    // -------------------------
    let overlap = false;
    for (const idx of schema.indices.availability) {
        if (vecA[idx] === 1 && vecB[idx] === 1) {
            overlap = true;
            break;
        }
    }
    if (!overlap) return true;

    return false; // no hard constraint failed
}

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
function computeCompatibility(vecA, vecB, weights, schema) {

    // Hard constraints first
    if (hardConstraintsFail(vecA, vecB, schema)) {
        return 0;
    }

    let score = 0;

    // Multi-choice fields (Jaccard)
    for (const field of schema.multi) {
        const subA = field.indices.map(i => vecA[i]);
        const subB = field.indices.map(i => vecB[i]);
        const j = jaccardIndex(subA, subB);
        score += weights[field.name] * j;
    }

    // Single-choice fields (delta)
    for (const field of schema.single) {
        const d = vecA[field.index] === vecB[field.index] ? 1 : 0;
        score += weights[field.name] * d;
    }

    return score;
}
 
module.exports=computeCompatibility;