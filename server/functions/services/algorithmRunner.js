const { onRequest } = require("firebase-functions/v2/https");
const admin = require("firebase-admin");
const functions = require("firebase-functions");
const { startRound } = require("../algorithm/roundManager");
const dbService = require("./firestoreDb");
const { createTask } = require("../tasks/taskService");

// ------------------------------------------------------------
// runAlgorithm
// Entry point for triggering a grouping round.
// Responsibilities:
//   - Update course status
//   - Run the algorithm for the current round
//   - Detect completion
//   - Schedule next round if needed
// ------------------------------------------------------------

exports.runAlgorithm = onRequest({
    memory: "512Mi",
    cpu: 1,
    timeoutSeconds: 300,
    minInstances: 0,
    invoker: "public" 
}, async(req, res) => {

    try {
        const { courseId, round } = req.body;
        if (!courseId || typeof round !== "number") {
            return res.status(400).send("Missing or invalid parameters");
        }

        const courseRef = admin.firestore().collection("courses").doc(courseId);

        // Mark course as in-progress and set current round
        await courseRef.update({ groupingStatus: "IN_PROGRESS" ,currentRound: round });

        // Run the algorithm for this round
        const stopGrouping = await startRound(courseId, dbService);

        // --------------------------------------------------------
        // Completion condition
        // --------------------------------------------------------
        if (stopGrouping) {

            await courseRef.update({
                groupingStatus: "COMPLETED"
            });

            return res.send("Finished");
        }

        // --------------------------------------------------------
        // Schedule next round
        // --------------------------------------------------------
        const nextRound = round + 1;
        
        // Schedule for 24 hours later
        const nextRun = new Date(Date.now() + 24 * 60 * 60 * 1000);

        const newTaskId = await createTask(courseId, nextRun, nextRound);

        await courseRef.update({
            taskId: newTaskId
        });

        res.send("Next round scheduled");

    } catch (error) {
        console.error("runAlgorithm error:", error);
        res.status(500).send("Internal error");
    }

});