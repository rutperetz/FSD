const fs = require("fs");

function randomChoice(arr) {
    return arr[Math.floor(Math.random() * arr.length)];
}

function randomSubset(arr) {
    return arr.filter(() => Math.random() > 0.5);
}

function generateStudents(n) {

    const students = [];

    for (let i = 0; i < n; i++) {

        students.push({
            studentId: "S" + i,
            answers: {
                gender: randomChoice(["male", "female"]),
                genderPreference: randomChoice(["men", "women", "no_preference"]),
                availability: randomSubset(["morning", "afternoon", "evening", "weekend"]),
                workStyle: randomSubset(["individual", "collaborative"]),
                workMode: randomSubset(["oncampus", "remote"]),
                language: randomSubset(["Hebrew", "English", "Arabic"]),
                taskPreference: randomSubset(["fixed", "flexible"])
            }
        });

    }

    return students;
}

function saveDataset(students) {

    fs.writeFileSync(
        "data.json",
        JSON.stringify({ students }, null, 2)
    );
}

module.exports = { generateStudents, saveDataset };