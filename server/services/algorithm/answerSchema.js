const schema = {
    indices: {
        gender: { male: 0, female: 1 },
        genderPref: { men: 2, women: 3, none: 4 },
        availability: [5, 6, 7, 8],
        workStyle: [9, 10],
        workMode: [11, 12],
        language: [13, 14, 15],
        taskPreference: [16, 17]
    },

    //  multi-choice (Jaccard)
    multi: [
        { name: "availability", indices: [5, 6, 7, 8] },
        { name: "workMode", indices: [11, 12] }
    ],

    // single-choice (delta)
    single: [
        { name: "workStyle", index: 9 },        // individual/collaborative
        { name: "language", index: 13 },        // Hebrew/English/Arabic (ניקח את הראשון)
        { name: "taskPreference", index: 16 }   // fixed/flexible
    ],
   
};
 module.exports = schema;