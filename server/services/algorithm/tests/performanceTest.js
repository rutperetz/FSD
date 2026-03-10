const { generateStudents, saveDataset } = require("./generateDataset");
const { startRound } = require("../window_clustering/roundManager");
const { performance } = require("perf_hooks");

function computeMetrics(result, totalStudents) {

    const groups = result.matchGroups || [];

    let totalMembers = 0;
    let totalScore = 0;

    groups.forEach(g => {
        totalMembers += g.memberIds.length;
        totalScore += g.groupScore || 0;
    });

    const assigned = totalMembers;

    const coverage = assigned / totalStudents;

    const avgGroupSize = groups.length ? totalMembers / groups.length : 0;

    const avgGroupScore = groups.length ? totalScore / groups.length : 0;

    return {
        assigned,
        coverage: coverage.toFixed(2),
        avgGroupSize: avgGroupSize.toFixed(2),
        avgGroupScore: avgGroupScore.toFixed(3)
    };
}

async function performanceTest() {

    console.log("LOAD + PERFORMANCE TEST");

    const sizes = [50, 100, 200, 400, 600];

    for (const n of sizes) {

        console.log("\nStudents:", n);

        const students = generateStudents(n);
        saveDataset(students);

        const start = performance.now();

        const result = await startRound("performanceCourse");

        const end = performance.now();

        const metrics = computeMetrics(result, n);

        console.log({
            students: n,
            runtime_ms: (end - start).toFixed(2),
            groups: result.matchGroups.length,
            unassigned: result.matchUnassigned.length,
            coverage: metrics.coverage,
            avgGroupSize: metrics.avgGroupSize,
            avgGroupScore: metrics.avgGroupScore
        });
    }
}

module.exports = performanceTest;