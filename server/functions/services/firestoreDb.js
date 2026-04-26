const admin = require("firebase-admin");

// ------------------------------------------------------------
// Firestore DB Service
// Provides all database operations required by the algorithm.
const dbService = {
    async getDb() {
        return admin.firestore();
    },

    // --------------------------------------------------------
    // Load course settings (group size + current round)
    // --------------------------------------------------------
    async getCourseSettings(courseId) {
        const db = await this.getDb();
        const doc = await db.collection("courses").doc(courseId).get();
        const data = doc.data();

        if (!data || !data.groupSize) {
            throw new Error(`Invalid course settings for course ${courseId}`);
        }

        return {
            minSize: data.groupSize.min,
            maxSize: data.groupSize.max,
            currentRound: data.currentRound
        };
    },


    // --------------------------------------------------------
    // ROUND 1: Load raw student answers (only opt-in students)
    // --------------------------------------------------------
    async getRawCourseData(courseId) {
        const db = await this.getDb();

        const enrollSnap = await db.collection("courses")
            .doc(courseId)
            .collection("enrollments")
            .get();

        const studentIds = enrollSnap.docs
            .filter(d => d.data().optIn === true)
            .map(d => d.data().studentId);

        const students = [];

        for (const id of studentIds) {
            if (!id) continue;
            const studentDoc = await db.collection("students").doc(id).get();
            const studentData = studentDoc.data();
            if (studentData) {
                students.push({
                    studentId: id,
                    answers: studentData.answers
                
                });
            }
            else {
            console.warn(` Student document with ID ${id} not found in 'students' collection!`);
            }
        }

        return students;
    },

    // --------------------------------------------------------
    // Save normalized vectors + index mapping
    // --------------------------------------------------------
    async saveNormalizedVectors(courseId, vectorsMap, idToIndex) {
        const db = await this.getDb();
        await db.collection("courses").doc(courseId).update({
            vectorsMap,
            idToIndex
        });
    },

    // --------------------------------------------------------
    // Load normalized vectors for later rounds
    // --------------------------------------------------------
    async getNormalizedVectors(courseId) {
        const db = await this.getDb();
        const doc = await db.collection("courses").doc(courseId).get();
        return {
            vectorsMap: doc.data().vectorsMap,
            idToIndex: doc.data().idToIndex
        };
    },

    // --------------------------------------------------------
    // Save round result (groups, unassigned, weights, feedback)
    // --------------------------------------------------------
    async saveRoundResult(courseId, roundData) {
        const db = await this.getDb();
        await db.collection("courses")
            .doc(courseId)
            .collection("matchRounds")
            .doc(roundData.roundId)
            .set(roundData);
    },

    // --------------------------------------------------------
    // Load previous round data
    // --------------------------------------------------------
    async getPreviousRound(courseId, roundNum) {
        const db = await this.getDb();
        const doc = await db.collection("courses")
            .doc(courseId)
            .collection("matchRounds")
            .doc(`${courseId}_R${roundNum}`)
            .get();

        return doc.exists ? doc.data() : null;
    },

    // --------------------------------------------------------
    // Build global blacklist from all rounds
    // Each rejection becomes a permanent "from → to" block
    // --------------------------------------------------------
    async getRejectionHistory(courseId) {
        const db = await this.getDb();
        const rounds = await db.collection("courses")
            .doc(courseId)
            .collection("matchRounds")
            .get();

        const blacklist = [];
        rounds.forEach(doc => {
            const r = doc.data();
            if (!r || !r.feedback) return;

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
