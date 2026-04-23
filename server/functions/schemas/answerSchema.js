// ------------------------------------------------------------
// Answer schema definition
// Defines all questionnaire fields, their types, and labels.
// Indices are generated dynamically to map each label to a
// unique position in the binary feature vector.
// ------------------------------------------------------------

const schema = {
    fields: {
        gender: {
            type: "single",
            labels: ["male", "female"]
        },

        genderPreference: {
            type: "single",
            labels: ["men", "women", "no_preference"]
        },

        availability: {
            type: "multi",
            labels: ["morning", "afternoon", "evening", "weekend"]
        },

        workStyle: {
            type: "multi",
            labels: ["individual", "collaborative"]
        },

        workMode: {
            type: "multi",
            labels: ["oncampus", "remote"]
        },

        language: {
            type: "multi",
            labels: ["Hebrew", "English", "Arabic"]
        },

        taskPreference: {
            type: "multi",
            labels: ["fixed", "flexible"]
        }
    },

    //added dynamically
    indices: {}
};

// -------------------------
// Build indices dynamically
// -------------------------
let currentIndex = 0;

for (const fieldName in schema.fields) {
    const field = schema.fields[fieldName];

    if (field.type === "single") {
        // single-choice → 
        field.indices = {};
        field.labels.forEach(label => {
            field.indices[label] = currentIndex++;
        });
    }

    if (field.type === "multi") {
        // multi-choice → 
        field.indices = [];
        field.labels.forEach(() => {
            field.indices.push(currentIndex++);
        });
    }
}

schema.indices = Object.fromEntries(
    Object.entries(schema.fields).map(([key, field]) => [key, field.indices])
);

module.exports = schema;
