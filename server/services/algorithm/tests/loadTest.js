const { generateStudents, saveDataset } = require("./generateDataset");
const { startRound } = require("../window_clustering/roundManager");
const { performance } = require("perf_hooks");

async function runLoadTest() {

    console.log("LOAD TEST");

    const sizes = [800, 1000];

    for (const n of sizes) {

        console.log("Students:", n);

        const students = generateStudents(n);
        saveDataset(students);

        const start = performance.now();

        const result = await startRound("loadCourse");

        const end = performance.now();


        console.log({
            students: n,
            time_ms: (end - start).toFixed(2),
            groups: result.matchGroups.length,
            unassigned: result.matchUnassigned.length
        });
    }
}

module.exports = runLoadTest;