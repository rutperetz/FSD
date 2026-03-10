const dbService = require('../db/dbService.js');
const buildSimilarityMatrix = require("../similarityMatrix.js");
const windowClustering = require("./windowClustering");
const { feedbackProcessor, lockGroups } = require("../feedbackProcessor.js");
const schema = require('../answerSchema.js');
const normalizeAnswers = require('../normalizeAnswers.js');

// -----------------------------------------------------------------
// פונקציות עזר להמרות בין IDs לאינדקסים במערך
// -----------------------------------------------------------------

// ממפה קבוצה מהאינדקסים שלה ל-IDs האמיתיים
function mapGroupToIds(group, idToIndex) {
    return {
        ...group,
        memberIds: group.memberIds.map(idx => idToIndex[idx])
    };
}

// ממפה פידבק (שהגיע עם IDs) חזרה לאינדקסים עבור האלגוריתם הפנימי
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
                .filter(idx => idx !== undefined), // סינון של סטודנטים שכבר לא קיימים
            rejectReasons: data.rejectReasons
        };
    }
    return feedbackWithIndices;
}

// -----------------------------------------------------------------
// הפונקציה המרכזית: מנהלת את הסבב, הנתונים והשמירה
// -----------------------------------------------------------------
async function startRound(courseId) {
    // 1. שליפת הגדרות הקורס מ-DB
    const settings = await dbService.getCourseSettings(courseId);
    const roundNum = settings.currentRound;
    const { minSize, maxSize, baseWeights } = settings;
    const strictThreshold = 0.7; // סף נוקשה לסיבוב הראשון

    console.log(`Starting Round ${roundNum} for course ${courseId}`);

    // 2. טיפול בווקטורים - שליפה מ-DB או יצירה ונרמול מחדש
    let vectorsData = await dbService.getNormalizedVectors(courseId);
    let vectorsArray = [];
    let idToIndex = [];

    if (!vectorsData) {
        console.log("No normalized vectors found. Generating mapping and vectors...");
        const rawStudents = await dbService.getRawCourseData(courseId);

        const vectorsMap = {};
        rawStudents.forEach(student => {
            vectorsMap[student.studentId] = normalizeAnswers(student.answers);
            idToIndex.push(student.studentId);
        });

       // await dbService.saveNormalizedVectors(courseId, vectorsMap, idToIndex);

        vectorsArray = idToIndex.map(id => vectorsMap[id]);
    } else {
        console.log("Loaded mapped vectors from DB.");
        idToIndex = vectorsData.idToIndex;
        vectorsArray = idToIndex.map(id => vectorsData.vectorsMap[id]);
    }

    const n = vectorsArray.length;
    let finalGroupsWithIds = [];
    let unassignedWithIds = [];
    let currentWeights = baseWeights;
    const roundId = `${courseId}_R${roundNum}`;

    // -----------------------------------------------------------------
    // לוגיקה לסבב ראשון (Round 1)
    // -----------------------------------------------------------------
    if (roundNum === 1) {
        // בניית מטריצה בסיסית - אין פידבקים קודמים או רשימות שחורות עדיין
        const matrix = buildSimilarityMatrix(vectorsArray, currentWeights, schema);
        const used = new Array(n).fill(false);

        // הרצה כפולה: קודם עם סף נוקשה, אח"כ חלון שאריות
        const { groups: groupsStrict, used: usedStrict } = windowClustering(vectorsArray, matrix, strictThreshold, minSize, maxSize, used);
        const { groups: groupsRelaxed, used: usedRelaxed } = windowClustering(vectorsArray, matrix, 0, minSize, maxSize, usedStrict);

        const allGroups = groupsStrict.concat(groupsRelaxed);
        const unassigned = Array.from({ length: n }, (_, idx) => idx).filter(idx => !usedRelaxed[idx]);

        // המרה ל-IDs לפני שמירה
        finalGroupsWithIds = allGroups.map(g => mapGroupToIds(g, idToIndex));
        unassignedWithIds = unassigned.map(idx => idToIndex[idx]);

    }
    // -----------------------------------------------------------------
    // לוגיקה לסבבים מתקדמים (Round 2+)
    // -----------------------------------------------------------------
    else {
        // א. שליפת נתוני עבר: סבב קודם, רשימה שחורה כוללת
        const previousRound = await dbService.getPreviousRound(courseId, roundNum - 1);
        if (!previousRound) throw new Error(`Cannot start round ${roundNum} - missing data from round ${roundNum - 1}`);

        // יצירת עותקים נקיים בזיכרון (Deep Copy) כדי לא לדרוס את ההיסטוריה בקובץ
        let matchGroupsWithIds = JSON.parse(JSON.stringify(previousRound.matchGroups));
        let matchWeights = { ...previousRound.matchWeights };
        letfeedbackFromUser = JSON.parse(JSON.stringify(previousRound.feedback));
        // ב. תרגום הפידבקים והקבוצות הקודמות בחזרה לאינדקסים של המטריצה
        const feedbackIndices = mapFeedbackToIndices(feedbackFromUser, idToIndex);

        // המרת הקבוצות הקודמות (שמורות כ-IDs) חזרה לאינדקסים כדי לבדוק נעילות
        const idToIndexMap = Object.fromEntries(idToIndex.map((id, idx) => [id, idx]));
        let previousGroupsAsIndices = matchGroupsWithIds.map(g => ({
            ...g,
            memberIds: g.memberIds.map(id => idToIndexMap[id]).filter(idx => idx !== undefined)
        }));

        // ג. עיבוד הפידבק: נעילת קבוצות (על סמך אינדקסים)
        previousGroupsAsIndices = lockGroups(previousGroupsAsIndices, feedbackIndices, minSize);


        // ד. עיבוד המשקלים החדשים ועדכון מטריצה
        // feedbackProcessor מחשב מטריצה מחדש ומחיל עליה דחיות ספציפיות
        const { newMatrix, updatedWeights } = feedbackProcessor(vectorsArray, previousGroupsAsIndices, feedbackIndices, matchWeights);
        currentWeights = updatedWeights;
        let matrix = newMatrix;

        // ה. החלת הרשימה השחורה המצטברת (מכל הסבבים הקודמים) על המטריצה החדשה
        const globalBlacklist = await dbService.getRejectionHistory(courseId);
        globalBlacklist.forEach(rejection => {
            const idxFrom = idToIndexMap[rejection.from];
            const idxTo = idToIndexMap[rejection.to];
            if (idxFrom !== undefined && idxTo !== undefined) {
                matrix[idxFrom][idxTo] = 0;
                matrix[idxTo][idxFrom] = 0;
            }
        });

        // ו. הרצת אלגוריתם השיבוץ רק על הסטודנטים הפנויים (!used)
        const { groups: groupsStrict, used: usedStrict } = windowClustering(vectorsArray, matrix, strictThreshold, minSize, maxSize, used);
        const { groups: groupsRelaxed, used: usedRelaxed } = windowClustering(vectorsArray, matrix, 0, minSize, maxSize, usedStrict);

        const newlyFormedGroups = groupsStrict.concat(groupsRelaxed);
        const combinedGroupsIndices = previousGroupsAsIndices.concat(newlyFormedGroups);
        const unassignedIndices = Array.from({ length: n }, (_, idx) => idx).filter(idx => !usedRelaxed[idx]);

        // ז. המרה סופית חזרה ל-IDs לקראת שמירה ל-DB
        finalGroupsWithIds = combinedGroupsIndices.map(g => mapGroupToIds(g, idToIndex));
        unassignedWithIds = unassignedIndices.map(idx => idToIndex[idx]);
    }

    // -----------------------------------------------------------------
    // אריזה, שמירה ל-DB וקידום הסבב הבא
    // -----------------------------------------------------------------
    const resultToSave = {
        roundId,
        courseId,
        roundNumber: roundNum,
        matchGroups: finalGroupsWithIds,
        matchUnassigned: unassignedWithIds,
        matchWeights: currentWeights,
        feedback: {} 
    };

    await dbService.saveRoundResult(courseId, resultToSave);
    //await dbService.updateCourseRound(courseId, roundNum + 1);

    console.log(`Round ${roundNum} completed successfully! Data saved.`);
    return resultToSave;
}

module.exports = { startRound };