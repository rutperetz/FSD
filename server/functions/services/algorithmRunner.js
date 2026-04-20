const { onRequest } = require("firebase-functions/v2/https");
const admin = require("firebase-admin");

const { startRound } = require("../algorithm/roundManager");
const dbService = require("./firestoreDb");
const { createTask } = require("../tasks/taskService");

exports.runAlgorithm = onRequest(async (req, res) => {

    try {
        const { courseId, round } = req.body;

        const courseRef = admin.firestore().collection("courses").doc(courseId);

        await courseRef.update({ groupingStatus: "IN_PROGRESS" ,currentRound: round });

        const result = await startRound(courseId, dbService);

        // -----------------------
        // תנאי סיום
        // -----------------------
        const { minSize } = await dbService.getCourseSettings(courseId);

        const totalStudents =
            result.matchGroups.reduce((sum, g) => sum + g.memberIds.length, 0) +
            result.matchUnassigned.length;

        const noGroups = result.matchGroups.length === 0;

        if (totalStudents < minSize || noGroups || result.roundNumber >= 3) {

            await courseRef.update({
                groupingStatus: "COMPLETED"
            });

            return res.send("Finished");
        }

        // -----------------------
        // סבב נוסף
        // -----------------------
        const nextRound = result.roundNumber + 1;
        const nextRun = new Date(Date.now() + 24 * 60 * 60 * 1000);

        const newTaskId = await createTask(courseId, nextRun, nextRound);

        await courseRef.update({
            currentRound: nextRound,
            taskId: newTaskId
        });

        res.send("Next round scheduled");

    } catch (e) {
        console.error(e);
        res.status(500).send("Error");
    }
});