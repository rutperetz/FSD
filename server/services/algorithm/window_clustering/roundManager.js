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
function roundManager(courseId, vectors, baseWeights, threshold, minSize, maxSize, roundNum, feedback) {
    const n = vectors.length;
    const id = courseId + roundNum;
    if (roundNum === 1) {
        const matchRounds = [];
        // 1. build similarity matrix
        const matrix = buildSimilarityMatrix(vectors, baseWeights, schema);

        const used = new Array(n).fill(false);

        // 2. run window clustering
        // First pass with strict threshold
        const { groups: groups_1, used: used_1 } = windowClustering(vectors, matrix,threshold, minSize, maxSize, used);
        //Second pass with relaxed threshold for unassigned students
        const { groups: groups_2, used: used_2 } = windowClustering(vectors, matrix, 0, minSize, maxSize, used_1);
        const groups = groups_1.concat(groups_2);

        const unassigned = Array.from({ length: used_2.length }, (_, idx) => idx)
            .filter(idx => !used_2[idx]);

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
        let { matchGroups, matchUnassigned,matchWeights } = previousRound; //לראות שלא מתנגש המשקלים, לא במבחן לקבל גם מערך תשובות
        
        //2. lock groups based on feedback
        matchGroups = lockGroups(matchGroups, feedback, minSize);
        used = new Array(n).fill(false);
        for (const group of matchGroups) {
            if (group.groupStatus) {
                for (const idx of group.memberIds) {
                    used[idx] = true;
                }

            }
        }
        // save unassigned students for next round
        for (const idx of matchUnassigned) {
            used[idx] = true;
        }
        
        let { newMatrix, updatedWeights } = feedbackProcessor(vectors, matchGroups,feedback, matchWeights);
        newWeights = updatedWeights;
        matrix = newMatrix;
        //3. run window clustering on students not used with updated matrix and weights
        // First pass with strict threshold
        const { groups: groups_1, used: used_1 } = windowClustering(vectors, matrix, threshold, minSize, maxSize, used);
        //Second pass with relaxed threshold for unassigned students
        const { groups: groups_2, used: used_2 } = windowClustering(vectors, matrix, 0, minSize, maxSize, used_1);
        const groups = groups_1.concat(groups_2);

        const unassigned = Array.from({ length: used_2.length }, (_, idx) => idx)
            .filter(idx => !used_2[idx]);
         const combinedUnassigned = unassigned.concat(matchUnassigned); // Add previously unassigned students back to the pool

        finalGroups = matchGroups.concat(groups);
            
        
        //4. save round data
        matchRounds.push({
            roundId: id,
            courseId,
            roundNumber: roundNum,
            matrix,
            matchGroups: finalGroups,
            matchUnassigned: combinedUnassigned,
            matchWeights: newWeights,
            feedback
        });
        saveMatchRounds(matchRounds);
        // return { IN_PROGRESS };
    }

}
module.exports = roundManager;