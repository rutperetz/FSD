const { generateStudents, saveDataset } = require("./generateDataset");
const { startRound } = require("../window_clustering/roundManager.js");

async function runEdgeTests() {

    console.log("Test: very small class");

    const students = generateStudents(2);

    saveDataset(students);

    const result = await startRound("edgeCourse");

    console.log(result);

    console.log("Test: identical students");
    const students2 = generateStudents(10);

    const identical = students2.map((s, i) => ({
        studentId: "X" + i,
        answers: students[0].answers
    }));

    saveDataset(identical);

    const result2 = await startRound("edgeCourse2");

    console.log(result2);

}

module.exports = runEdgeTests;