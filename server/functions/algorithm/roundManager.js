
const buildSimilarityMatrix = require("./helpers/similarityMatrix.js");
const windowClustering = require("./windowClustering.js");
const { feedbackProcessor, lockGroups } = require("./helpers/feedbackProcessor.js");
const normalizeAnswers = require('./helpers/normalizeAnswers.js');
const algorithmConfig = require("../config/algorithmConfig.js");
const e = require("express");


// ------------------------------------------------------------
// Utility: Map group member indices → student IDs
// ------------------------------------------------------------
function mapGroupToIds(group, idToIndex) {
    return {
        ...group,
        memberIds: group.memberIds.map(idx => idToIndex[idx])
    };
}

// ------------------------------------------------------------
// Utility: Convert feedback with student IDs → indices
// ------------------------------------------------------------
function mapFeedbackToIndices(feedbackWithIds, idToIndex) {
    if (!feedbackWithIds) return {};
    const feedbackWithIndices = {};
    const idToIndexMap = Object.fromEntries(idToIndex.map((id, idx) => [id, idx]));

    for (const [studentId, data] of Object.entries(feedbackWithIds)) {
        const studentIdx = idToIndexMap[studentId];
        if (studentIdx === undefined) continue;

        feedbackWithIndices[studentIdx] = {
            approveGroup: data.approveGroup,
            rejectStudents: data.rejectStudents
                .map(rId => idToIndexMap[rId])
                .filter(idx => idx !== undefined), 
            rejectReasons: data.rejectReasons
        };
    }
    return feedbackWithIndices;
}

// -----------------------------------------------------------------
// The main function: manages the round, data and storage
// -----------------------------------------------------------------
async function startRound(courseId, dbService) {
    // Load course settings
    const settings = await dbService.getCourseSettings(courseId);
    if (!settings) throw new Error(`Missing course settings for ${courseId}`);

    const roundNum = settings.currentRound;
    const { minSize, maxSize } = settings;
    const strictThreshold = algorithmConfig.threshold;

    console.log(`Starting Round ${roundNum} for course ${courseId}`);

    // Internal state
    let vectorsArray = [];
    let idToIndex = [];
    let finalGroupsWithIds = [];
    let unassignedWithIds = [];
    let currentWeights = algorithmConfig.weights;
    const roundId = `${courseId}_R${roundNum}`;
    let stopGrouping = false;

    // ------------------------------------------------------------
    // ROUND 1 — Build vectors and initial similarity matrix
    // ------------------------------------------------------------
    if (roundNum === 1) {
        const rawStudents = await dbService.getRawCourseData(courseId);

        if (!rawStudents || rawStudents.length === 0) {
            console.log("No students found. Marking as COMPLETED.");
            stopGrouping = true;
        }
       
        if (rawStudents.length < minSize) {
            console.log(`Not enough students to form groups. Minimum required is ${minSize}, but got ${rawStudents.length}. Marking as COMPLETED.`);
            unassignedWithIds = rawStudents.map(s => s.studentId);
            stopGrouping = true;
        }

        else {
            console.log(" Generating mapping and vectors...")
            const vectorsMap = {};
            rawStudents.forEach(student => {
                vectorsMap[student.studentId] = normalizeAnswers(student.answers);
                idToIndex.push(student.studentId);
            });

            await dbService.saveNormalizedVectors(courseId, vectorsMap, idToIndex);

            vectorsArray = idToIndex.map(id => vectorsMap[id]);
            const n = vectorsArray.length;
            const matrix = buildSimilarityMatrix(vectorsArray, currentWeights);
            const used = new Array(n).fill(false);

            // Strict pass
            const { groups: strictGroups , used: usedStrict } = windowClustering(vectorsArray, matrix, strictThreshold, minSize, maxSize, used);
            // Relaxed pass
            const { groups: relaxedGroups , used: usedRelaxed } = windowClustering(vectorsArray, matrix, 0, minSize, maxSize, usedStrict);

            const allGroups = strictGroups .concat(relaxedGroups );
            const unassigned = Array.from({ length: n }, (_, idx) => idx).filter(idx => !usedRelaxed[idx]);

            finalGroupsWithIds = allGroups.map(g => mapGroupToIds(g, idToIndex));
            unassignedWithIds = unassigned.map(idx => idToIndex[idx]);
        }
    }
        // ------------------------------------------------------------
        // ROUND 2+ — Apply feedback, lock groups, re-cluster remaining
        // ------------------------------------------------------------
    else {
        let vectorsData = await dbService.getNormalizedVectors(courseId);
        
        if (!vectorsData) throw new Error(`Missing normalized vectors for ${courseId}`);
            
        else {
            console.log("Loaded mapped vectors from DB.");
            idToIndex = vectorsData.idToIndex;
            vectorsArray = idToIndex.map(id => vectorsData.vectorsMap[id]);
        }

        const n = vectorsArray.length;
        const previousRound = await dbService.getPreviousRound(courseId, roundNum - 1);
        if (!previousRound) throw new Error(`Cannot start round ${roundNum} - missing data from round ${roundNum - 1}`);

      
        let matchGroupsWithIds = JSON.parse(JSON.stringify(previousRound.matchGroups));
        let matchWeights = { ...previousRound.matchWeights };
        let matchUnassigned= JSON.parse(JSON.stringify(previousRound.matchUnassigned));
        let feedbackFromUser = JSON.parse(JSON.stringify(previousRound.feedback));
        const feedbackIndices = mapFeedbackToIndices(feedbackFromUser, idToIndex);

     
        const idToIndexMap = Object.fromEntries(idToIndex.map((id, idx) => [id, idx]));
        let previousGroupsAsIndices = matchGroupsWithIds.map(g => ({
            ...g,
            memberIds: g.memberIds.map(id => idToIndexMap[id]).filter(idx => idx !== undefined)
        }));

        // Lock groups based on feedback
        lockPreviousGroups = lockGroups(previousGroupsAsIndices, feedbackIndices, minSize);
        
        const used = new Array(n).fill(false);
        lockPreviousGroups.forEach(g => {
            g.memberIds.forEach(idx => { used[idx] = true; });
        });

        // If all groups locked → marking as COMPLETED
        if ((lockPreviousGroups.length === previousGroupsAsIndices.length) && (matchUnassigned.length === used.filter(x => !x).length)) { 
            finalGroupsWithIds = finalGroupsWithIds = lockPreviousGroups.map(g => mapGroupToIds(g, idToIndex));
            unassignedWithIds = matchUnassigned;
            currentWeights = matchWeights
            stopGrouping = true;
             console.log("All groups locked. Marking as COMPLETED.");
        }
        else {

            // Rebuild matrix with feedback (rejectReasons currently inactive)
            const globalBlacklist = await dbService.getRejectionHistory(courseId);

            const { newMatrix, updatedWeights } =
                feedbackProcessor(
                    vectorsArray,
                    previousGroupsAsIndices,
                    feedbackIndices,
                    matchWeights,
                    globalBlacklist,
                    idToIndexMap
                );
            
            currentWeights = updatedWeights;
            let matrix = newMatrix;

            // Re-cluster only unlocked students
            const { groups: strictGroups, used: usedStrict } = windowClustering(vectorsArray, matrix, strictThreshold, minSize, maxSize, used);
            const { groups: relaxedGroups , used: usedRelaxed } = windowClustering(vectorsArray, matrix, 0, minSize, maxSize, usedStrict);

            const newlyFormedGroups = strictGroups.concat(relaxedGroups );
            const combinedGroupsIndices = lockPreviousGroups.concat(newlyFormedGroups);
            const unassignedIndices = Array.from({ length: n }, (_, idx) => idx).filter(idx => !usedRelaxed[idx]);

           
            finalGroupsWithIds = combinedGroupsIndices.map(g => mapGroupToIds(g, idToIndex));
            unassignedWithIds = unassignedIndices.map(idx => idToIndex[idx]);
        }
        // Marking as COMPLETED if reached max rounds
        if (roundNum === algorithmConfig.maxRounds) {
            finalGroupsWithIds = finalGroupsWithIds.map(g => ({ ...g, groupStatus: true }));
            stopGrouping = true;
            console.log("Reached max rounds. Marking as COMPLETED.");
        }
    }

    // ------------------------------------------------------------
    // Build initial feedback for next round
    // ------------------------------------------------------------
    const initialFeedback = {};
    idToIndex.forEach(studentId => {
        initialFeedback[studentId] = {
            approveGroup: true,
            rejectStudents: [],
            rejectReasons: {}  // Feature reserved for future expansion
        };
    });


    // ------------------------------------------------------------
    // Save round result
    // ------------------------------------------------------------
    const resultToSave = {
        roundId,
        courseId,
        roundNumber: roundNum,
        matchGroups: finalGroupsWithIds,
        matchUnassigned: unassignedWithIds,
        matchWeights: currentWeights,
        feedback: initialFeedback
    };

 if (finalGroupsWithIds.length === 0) {
        console.log("No groups formed this round. Marking as COMPLETED.");
        stopGrouping = true;
    }
    await dbService.saveRoundResult(courseId, resultToSave);
   

    console.log(`Round ${roundNum} completed successfully! Data saved.`);
    return stopGrouping ;
}

module.exports = { startRound };