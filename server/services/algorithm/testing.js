
const normalizeAnswers = require('./normalizeAnswers.js');
const buildSimilarityMatrix = require('./similarityMatrix.js');
const fs = require("fs");
const path = require("path");
const { explainGroupReason, computeGroupScore } = require('./groupReasonAndScore');
const windowClustering = require('./window_clustering/windowClustering.js');
const schema = require('./answerSchema.js');
const roundManager = require('./window_clustering/roundManager.js');

weights = {
    availability: 0.4,
    workMode: 0.1,
    workStyle: 0.2,
    language: 0.15,
    taskPreference: 0.15
}
minSize = 2;
maxSize = 3;
courseId = "CS101";

function loadStudentsFromFile() {
    const filePath = path.join(__dirname, './data_updated.json');
    const raw = fs.readFileSync(filePath, "utf8");
    const data = JSON.parse(raw);

    const studentsMap = new Map();
    const vectors = [];


    data.students.forEach(student => {
        const vector = normalizeAnswers(student.answers);
        studentsMap.set(student.studentId, {
            vector
        });
        vectors.push(vector);
    });

    const feedback_1 = data.feedbackRound_1;
    const feedback_2 = data.feedbackRound_2;



    return { feedback_1, feedback_2, vectors };
}

// test-1 normalizeAnswers + similarityScore
function test1() {

    const { feedback_1, feedback_2, vectors } = loadStudentsFromFile();
    console.log("normalizeAnswers:", vectors);
}

//test-2 similarityMatrix
function test2() {
    const { feedback_1, feedback_2, vectors } = loadStudentsFromFile();
    const matrix = buildSimilarityMatrix(vectors, weights, schema);
    console.log(
        JSON.stringify("vectors:" + vectors, null, 2)
    );
    console.log(
        JSON.stringify("matrix:" + matrix, null, 2)
    );
}
// test-3 groupReason + groupScore
function test3() {
    const { feedback_1, feedback_2, vectors } = loadStudentsFromFile();
    const matrix = buildSimilarityMatrix(vectors, weights, schema);
    groupIndices = [0, 9, 6];
    const reasons = explainGroupReason(groupIndices, vectors, schema);
    console.log(reasons);
    const score = computeGroupScore(groupIndices, matrix);
    console.log("group score:", score);

}

// test-4 windowClustering
function test4() {
    used = new Array(14).fill(false);
    const { feedback_1, feedback_2, vectors } = loadStudentsFromFile();
    const matrix = buildSimilarityMatrix(vectors, weights, schema);
    const threshold = 0.7;
    const { groups, newUsed } = windowClustering(vectors, matrix,threshold, minSize, maxSize, used);

    fs.writeFileSync(
        "groups_output.json",
        JSON.stringify(groups, null, 2),
        "utf8"
    );

    console.log("Groups written to groups_output.json");

    console.log(
        JSON.stringify("newUsed:" + newUsed, null, 2)
    );
}
// test-5 roundManager
function test5() {
    const { feedback_1, feedback_2, vectors } = loadStudentsFromFile();
    const threshold = 0.7;
    roundManager(courseId, vectors, weights, threshold, minSize, maxSize, 1, null);
    //const groupingStatus = roundManager(courseId, vectors, weights, minSize, maxSize, 1, null);
    // console.log("Grouping Status:",groupingStatus);
    roundManager(courseId, vectors, weights, 0.6, minSize, maxSize, 2, feedback_1);
    roundManager(courseId, vectors, weights, 0.5, minSize, maxSize, 3, feedback_2);
}

//test1();
//test2();
//test3();
//test4();
test5();