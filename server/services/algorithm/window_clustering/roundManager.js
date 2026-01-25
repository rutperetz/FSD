const fs = require('fs');
const path = require('path');
const FILE_PATH = path.join(__dirname, 'matchRounds.json');
const buildSimilarityMatrix = require("./similarityMatrix");
const windowClustering = require("./windowClustering");
const feedbackProcessor = require("./feedbackProcessor");
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
function roundManager(courseId, vectors, baseWeights, minSize, maxSize, roundNum,feedback) {
    const n = vectors.length;
    const id = courseId + roundNum;
    if (roundNum === 1) {
        const matchRounds = [];
        // 1. build similarity matrix
        const matrix= buildSimilarityMatrix(vectors, baseWeights, schema);
     
        const used = new Array(n).fill(false);

        // 2. run window clustering

        const { groups, unassigned} = windowClustering(vectors, matrix, minSize, maxSize, used, schema);
        //TODO mapping the group members to student ids with student map
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
    }
    else {
        //1. load previous round data
        const matchRounds = loadMatchRounds();
        const previousRound = matchRounds.find(r => r.courseId === courseId && r.roundNumber === roundNum - 1);
        let { matchGroups, matchWeights } = previousRound; //לראות שלא מתנגש המשקלים, לא במבחן לקבל גם מערך תשובות
        //TODO mapping the group members and rejectStudents to index with student map
        //2. process feedback
        const used = new Array(n).fill(false);
        let { matrix, lockedGroups, newWeights } = feedbackProcessor(matchGroups, feedback, minSize, used, n, matchWeights);
        //3. run window clustering again
        const { groups, unassigned } = windowClustering(vectors, matrix, minSize, maxSize, used, schema);
        //merge locked groups and new groups
        let finalGroups = lockedGroups.concat(groups);
        //TODO mapping the group members to student ids with student map
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
    }
   
}
module.exports = roundManager;