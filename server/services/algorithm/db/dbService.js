const fs = require('fs');
const path = require('path');

const STUDENTS_PATH = path.join(__dirname, '../tests/data.json');
const VECTORS_PATH = path.join(__dirname, '../tests/course_vectors.json');
const ROUNDS_PATH = path.join(__dirname, '../tests/matchRounds.json');
const SETTINGS_PATH = path.join(__dirname, '../tests/courseSettings.json');

const dbService = {
    // ---------------------------------------------------------
    // שליפת הגדרות קורס מעודכנות
    // ---------------------------------------------------------
    getCourseSettings: async (courseId) => {
        // ב-Firebase: await db.collection('courses').doc(courseId).get()
        if (fs.existsSync(SETTINGS_PATH)) {
            const allSettings = JSON.parse(fs.readFileSync(SETTINGS_PATH, 'utf8'));
            if (allSettings[courseId]) return allSettings[courseId];
        }
        // ערכי ברירת מחדל במידה ולא הוגדר עדיין
        return {
            minSize: 3,
            maxSize: 4,
            baseWeights: { availability: 0.4, workMode: 0.1, workStyle: 0.2, language: 0.15, taskPreference: 0.15 },
            currentRound: 1
        };
    },

    // ---------------------------------------------------------
    // עדכון מספר סבב הקורס לאחר הרצה
    // ---------------------------------------------------------
    updateCourseRound: async (courseId, nextRoundNum) => {
        // ב-Firebase: await db.collection('courses').doc(courseId).update({ currentRound: nextRoundNum })
        let allSettings = {};
        if (fs.existsSync(SETTINGS_PATH)) {
            allSettings = JSON.parse(fs.readFileSync(SETTINGS_PATH, 'utf8'));
        }
        if (!allSettings[courseId]) allSettings[courseId] = await dbService.getCourseSettings(courseId);
        allSettings[courseId].currentRound = nextRoundNum;
        fs.writeFileSync(SETTINGS_PATH, JSON.stringify(allSettings, null, 2));
    },

    // ---------------------------------------------------------
    // שליפת הנתונים הגולמיים (Raw Students)
    // ---------------------------------------------------------
    getRawCourseData: async (courseId) => {
        // ב-Firebase: שליפה של מסמך הקורס או אוסף הסטודנטים בו
        const raw = fs.readFileSync(STUDENTS_PATH, 'utf8');
        const data = JSON.parse(raw);
        // בדוגמה הזו אנו מחזירים את כל הסטודנטים תחת courseId ספציפי (בהנחה שזה מסונן)
        return data.students;
    },

    // ---------------------------------------------------------
    // ניהול וקטורים: שמירה ושליפה
    // ---------------------------------------------------------
    saveNormalizedVectors: async (courseId, vectorsMap, idToIndex) => {
        let allVectors = {};
        if (fs.existsSync(VECTORS_PATH)) {
            allVectors = JSON.parse(fs.readFileSync(VECTORS_PATH, 'utf8'));
        }
        allVectors[courseId] = { vectorsMap, idToIndex };
        fs.writeFileSync(VECTORS_PATH, JSON.stringify(allVectors, null, 2));
        console.log(`Saved vectors and id-mapping for course ${courseId}`);
    },

    getNormalizedVectors: async (courseId) => {
        if (!fs.existsSync(VECTORS_PATH)) return null;
        const allVectors = JSON.parse(fs.readFileSync(VECTORS_PATH, 'utf8'));
        return allVectors[courseId] || null; // מחזיר אובייקט של { vectorsMap, idToIndex }
    },

    // ---------------------------------------------------------
    // ניהול סבבים: שמירה, שליפת סבב קודם ושליפת היסטוריית דחיות
    // ---------------------------------------------------------
    saveRoundResult: async (courseId, roundData) => {
        let allRounds = [];
        if (fs.existsSync(ROUNDS_PATH)) {
            allRounds = JSON.parse(fs.readFileSync(ROUNDS_PATH, 'utf8'));
        }
        allRounds.push(roundData);
        fs.writeFileSync(ROUNDS_PATH, JSON.stringify(allRounds, null, 2));
    },

    getPreviousRound: async (courseId, targetRoundNum) => {
        if (!fs.existsSync(ROUNDS_PATH)) return null;
        const allRounds = JSON.parse(fs.readFileSync(ROUNDS_PATH, 'utf8'));
        // חיפוש הסבב הספציפי שביקשנו (roundNum - 1)
        return allRounds.find(r => r.courseId === courseId && r.roundNumber === targetRoundNum);
    },

    getRejectionHistory: async (courseId) => {
        if (!fs.existsSync(ROUNDS_PATH)) return [];
        const rounds = JSON.parse(fs.readFileSync(ROUNDS_PATH, 'utf8'));

        const blacklist = [];
        rounds.filter(r => r.courseId === courseId).forEach(round => {
            Object.entries(round.feedback || {}).forEach(([studentId, fb]) => {
                fb.rejectStudents.forEach(rejectedId => {
                    blacklist.push({ from: studentId, to: rejectedId });
                });
            });
        });
        return blacklist;
    }
};

module.exports = dbService;