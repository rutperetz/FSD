
function normalizeAnswers(ans) {
    const vector = [];

    // -------------------------
    // gender: male / female
    // -------------------------
    vector.push(ans.gender === "male" ? 1 : 0);     // gender_male
    vector.push(ans.gender === "female" ? 1 : 0);   // gender_female

    // -------------------------
    // genderPreference: men / women / no_preference
    // -------------------------
    vector.push(ans.genderPreference === "men" ? 1 : 0);             // pref_men
    vector.push(ans.genderPreference === "women" ? 1 : 0);           // pref_women
    vector.push(ans.genderPreference === "no_preference" ? 1 : 0);   // pref_none

    // -------------------------
    // availability: morning / afternoon / evening / weekend
    // -------------------------
    vector.push(ans.availability.includes("morning") ? 1 : 0);    // avail_morning
    vector.push(ans.availability.includes("afternoon") ? 1 : 0);  // avail_afternoon
    vector.push(ans.availability.includes("evening") ? 1 : 0);    // avail_evening
    vector.push(ans.availability.includes("weekend") ? 1 : 0);    // avail_weekend

    // -------------------------
    // workStyle: individual / collaborative
    // -------------------------
    vector.push(ans.workStyle === "individual" ? 1 : 0);       // style_individual
    vector.push(ans.workStyle === "collaborative" ? 1 : 0);    // style_collaborative

    // -------------------------
    // workMode: oncampus / remote
    // -------------------------
    vector.push(ans.workMode.includes("oncampus") ? 1 : 0);    // mode_oncampus
    vector.push(ans.workMode.includes("remote") ? 1 : 0);      // mode_remote

    // -------------------------
    // language: Hebrew / English / Arabic
    // -------------------------
    vector.push(ans.language === "Hebrew" ? 1 : 0);   // lang_hebrew
    vector.push(ans.language === "English" ? 1 : 0);  // lang_english
    vector.push(ans.language === "Arabic" ? 1 : 0);   // lang_arabic

    // -------------------------
    // taskPreference: fixed / flexible
    // -------------------------
    vector.push(ans.taskPreference === "fixed" ? 1 : 0);     // task_fixed
    vector.push(ans.taskPreference === "flexible" ? 1 : 0);  // task_flexible

    return vector;
}

module.exports = normalizeAnswers;