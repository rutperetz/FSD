const runEdgeTests = require("./edgeCasesTest");
const runPerformance = require("./performanceTest");
const runLoadTest = require("./loadTest");
const runPruningTest = require("./pruningTest");

async function runAllTests() {

    console.log("\n=======================");
    console.log("EDGE CASE TESTS");
    console.log("=======================\n");

    await runEdgeTests();

    console.log("\n=======================");
    console.log("PERFORMANCE TESTS");
    console.log("=======================\n");

    await runPerformance();

    console.log("\n=======================");
    console.log("LOAD TEST");
    console.log("=======================\n");

    await runLoadTest();

    // console.log("\n=======================");
    // console.log("PRUNING TEST");
    // console.log("=======================\n");

    //  await runPruningTest();

    console.log("\n=======================");
    console.log("ALL TESTS FINISHED");
    console.log("=======================\n");
}

runAllTests();