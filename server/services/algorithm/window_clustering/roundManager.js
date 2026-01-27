const fs = require('fs');
const path = require('path');
const FILE_PATH = path.join(__dirname, 'matchRounds.json');
const buildSimilarityMatrix = require("../similarityMatrix.js");
const windowClustering = require("./windowClustering");
const { feedbackProcessor, lockGroups } = require("../feedbackProcessor.js");
const schema = require('../answerSchema.js');


function loadMatchRounds() {
    if (!fs.existsSync(FILE_PATH)) return [];
    const raw = fs.readFileSync(FILE_PATH, 'utf8');
    return JSON.parse(raw);
}

function saveMatchRounds(matchRounds) {
    fs.writeFileSync(FILE_PATH, JSON.stringify(matchRounds, null, 2), 'utf8');
}


//feedback- רק לצורך מבחן
function roundManager(courseId, vectors, baseWeights, minSize, maxSize, roundNum, feedback) {
    const n = vectors.length;
    const id = courseId + roundNum;
    if (roundNum === 1) {
        const matchRounds = [];
        // 1. build similarity matrix
        const matrix = buildSimilarityMatrix(vectors, baseWeights, schema);

        const used = new Array(n).fill(false);

        // 2. run window clustering

        const { groups, unassigned } = windowClustering(vectors, matrix, minSize, maxSize, used, schema);

        // 3. save round data
        matchRounds.push({
            roundId: id,
            courseId,
            roundNumber: roundNum,
            matrix,
            matchGroups: groups,
            matchUnassigned: unassigned,
            matchWeights: baseWeights,
            feedback  //לא במבחן- מחזיר ערך ריק ומתמלר=א ע משוב מהסטודנטים  
        });
        saveMatchRounds(matchRounds);
        // return { IN_PROGRESS };
    }
    else {
        //1. load previous round data
        const matchRounds = loadMatchRounds();
        const previousRound = matchRounds.find(r => r.courseId === courseId && r.roundNumber === roundNum - 1);
        let { matchGroups, matchWeights } = previousRound; //לראות שלא מתנגש המשקלים, לא במבחן לקבל גם מערך תשובות
        // separate locked groups
        trueGroups = matchGroups.filter(g => g.groupStatus); // keep only locked groups
        matchGroups = matchGroups.filter(g => !g.groupStatus); // keep only unlocked groups
        matchGroups = lockGroups(matchGroups, feedback, minSize);
        trueGroups = trueGroups.concat(matchGroups.filter(g => g.groupStatus)); // update true groups with newly locked groups
        let unmatchGroups = matchGroups.filter(g => !g.groupStatus); // keep only still unlocked groups

        const matchStudent = new Array(n).fill(false);
        for (const group of trueGroups) {
            for (const memberIdx of group.memberIds) {
                matchStudent[memberIdx] = true;
            }
        }
        let finalGroups = [];
        let unassigned = [];
        let matrix;
        let newWeights;

        if (matchStudent.every(Boolean)) {
            finalGroups = trueGroups;
            unassigned = [];
        } else {
            const unmatchVectors = [];
            const indexMap = [];

            vectors.forEach((vector, originalIndex) => {
                if (!matchStudent[originalIndex]) {
                    indexMap.push(originalIndex);
                    unmatchVectors.push(vector);
                }
            });

            const filteredFeedback = Object.fromEntries(
                Object.entries(feedback).filter(([idx]) => !matchStudent[idx])
            );

            let { newMatrix, updatedWeights } =
                feedbackProcessor(unmatchVectors, unmatchGroups, filteredFeedback, matchWeights);

            newWeights = updatedWeights
            matrix = newMatrix;
            const { groups, unassigned: newUnassigned } =
                windowClustering(unmatchVectors, matrix, minSize, maxSize);

            for (const group of groups) {
                group.memberIds = group.memberIds.map(idx => indexMap[idx]);
            }

            finalGroups = trueGroups.concat(groups);
            unassigned = newUnassigned.map(idx => indexMap[idx]);
        }
        //4. save round data
        matchRounds.push({
            roundId: id,
            courseId,
            roundNumber: roundNum,
            matrix,
            matchGroups: finalGroups,
            matchUnassigned: unassigned,
            matchWeights: newWeights,
            feedback
        });
        saveMatchRounds(matchRounds);
        // return { IN_PROGRESS };
    }

}
module.exports = roundManager;