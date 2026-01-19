const schema = require('./answerSchema');

function explainGroupReason(groupIndices, vectors) {
    const result = {
        gender: null,
        availability: [],
        workMode: [],
        workStyle: null,
        language: null,
        taskPreference: null
    };

    // -------------------------
    // Gender
    // -------------------------
    const allFemale = groupIndices.every(i => vectors[i][schema.indices.gender.female] === 1);
    const allMale = groupIndices.every(i => vectors[i][schema.indices.gender.male] === 1);

    if (allFemale) result.gender = "female";
    if (allMale) result.gender = "male";

    // -------------------------
    // Availability (multi)
    // -------------------------
    const availabilityLabels = ["morning", "afternoon", "evening", "weekend"];
    const availabilityIdx = schema.indices.availability;

    availabilityLabels.forEach((label, idx) => {
        const bitIndex = availabilityIdx[idx];
        const allHave = groupIndices.every(i => vectors[i][bitIndex] === 1);
        if (allHave) result.availability.push(label);
    });

    // -------------------------
    // Work Mode (multi)
    // -------------------------
    const workModeLabels = ["oncampus", "remote"];
    const workModeIdx = schema.indices.workMode;

    workModeLabels.forEach((label, idx) => {
        const bitIndex = workModeIdx[idx];
        const allHave = groupIndices.every(i => vectors[i][bitIndex] === 1);
        if (allHave) result.workMode.push(label);
    });

    // -------------------------
    // Work Style (single)
    // -------------------------
    const allIndiv = groupIndices.every(i => vectors[i][schema.indices.workStyle[0]] === 1);
    const allCollab = groupIndices.every(i => vectors[i][schema.indices.workStyle[1]] === 1);

    if (allIndiv) result.workStyle = "individual";
    if (allCollab) result.workStyle = "collaborative";

    // -------------------------
    // Language (single)
    // -------------------------
    const langLabels = ["Hebrew", "English", "Arabic"];
    const langIdx = schema.indices.language;

    langLabels.forEach((label, idx) => {
        const bitIndex = langIdx[idx];
        const allSpeak = groupIndices.every(i => vectors[i][bitIndex] === 1);
        if (allSpeak) result.language = label;
    });

    // -------------------------
    // Task Preference (single)
    // -------------------------
    const allFixed = groupIndices.every(i => vectors[i][schema.indices.taskPreference[0]] === 1);
    const allFlexible = groupIndices.every(i => vectors[i][schema.indices.taskPreference[1]] === 1);

    if (allFixed) result.taskPreference = "fixed";
    if (allFlexible) result.taskPreference = "flexible";

    return result;
}

module.exports = explainGroupReason;