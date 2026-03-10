const { generateStudents, saveDataset } = require("./generateDataset");
const { startRound } = require("../window_clustering/roundManager");
async function runPruningTest() {
    const students = generateStudents(150);
    saveDataset(students);
    const result = await startRound("pruningCourse");
}

module.exports = runPruningTest;