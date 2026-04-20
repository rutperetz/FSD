const admin = require("firebase-admin");
const db = admin.firestore();

const dbService = {

    async getCourseSettings(courseId) {
        const doc = await db.collection("courses").doc(courseId).get();
        const data = doc.data();

        return {
            minSize: data.groupSize.min,
            maxSize: data.groupSize.max,
            currentRound: data.currentRound
        };
    },

    // ---------------------------
    // ROUND 1 DATA
    // ---------------------------
    async getRawCourseData(courseId) {

        const enrollSnap = await db.collection("courses")
            .doc(courseId)
            .collection("enrollments")
            .get();

        const studentIds = enrollSnap.docs
            .filter(d => d.data().optIn === true)
            .map(d => d.id);

        const students = [];

        for (const id of studentIds) {
            const studentDoc = await db.collection("students").doc(id).get();
            students.push({
                studentId: id,
                answers: studentDoc.data().answers
            });
        }

        return students;
    },

    async saveNormalizedVectors(courseId, vectorsMap, idToIndex) {
        await db.collection("courses").doc(courseId).update({
            vectorsMap,
            idToIndex
        });
    },

    async getNormalizedVectors(courseId) {
        const doc = await db.collection("courses").doc(courseId).get();
        return {
            vectorsMap: doc.data().vectorsMap,
            idToIndex: doc.data().idToIndex
        };
    },

    // ---------------------------
    // ROUNDS
    // ---------------------------
    async saveRoundResult(courseId, roundData) {

        await db.collection("courses")
            .doc(courseId)
            .collection("matchRounds")
            .doc(roundData.roundId)
            .set(roundData);
    },

    async getPreviousRound(courseId, roundNum) {
        const doc = await db.collection("courses")
            .doc(courseId)
            .collection("matchRounds")
            .doc(`${courseId}_R${roundNum}`)
            .get();

        return doc.exists ? doc.data() : null;
    },

    async getRejectionHistory(courseId) {
        const rounds = await db.collection("courses")
            .doc(courseId)
            .collection("matchRounds")
            .get();

        const blacklist = [];

        rounds.forEach(doc => {
            const r = doc.data();

            Object.entries(r.feedback || {}).forEach(([studentId, fb]) => {
                fb.rejectStudents.forEach(target => {
                    blacklist.push({ from: studentId, to: target });
                });
            });
        });

        return blacklist;
    }
};

module.exports = dbService;
