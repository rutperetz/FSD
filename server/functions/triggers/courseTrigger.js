const { onDocumentWritten } = require("firebase-functions/v2/firestore");
const { createTask, deleteTask } = require("../tasks/taskService");

// ------------------------------------------------------------
// Firestore Trigger: handleCourseDeadlineChange
// Triggered whenever a course document is created, updated, or deleted.
// Responsibilities:
//   - Detect deadline changes
//   - Delete old Cloud Tasks
//   - Create new Cloud Tasks
//   - Update Firestore with new taskId
//   - Support both emulator and production environments
// ------------------------------------------------------------
exports.handleCourseDeadlineChange = onDocumentWritten(
    "courses/{courseId}",
    async (event) => {
        console.log("Trigger execution started");

        const courseId = event.params.courseId;

        // Extract before/after snapshots safely
        const beforeSnapshot = event.data?.before;
        const afterSnapshot = event.data?.after;

        const beforeData = beforeSnapshot?.exists ? beforeSnapshot.data() : null;
        const afterData = afterSnapshot?.exists ? afterSnapshot.data() : null;

        /// --------------------------------------------------------
        // 1. Document deleted → clean up task
        // --------------------------------------------------------
        if (!afterData) {
            console.log(`Document ${courseId} deleted.`);
            if (beforeData?.taskId) {
                console.log(`Cleaning up task: ${beforeData.taskId}`);
                await deleteTask(beforeData.taskId);
            }
            return;
        }

        // --------------------------------------------------------
        // Helper: Convert Firestore Timestamp or raw value to Date
        // --------------------------------------------------------
        const convertToDate = (val) => {
            if (!val) return null;
            if (val.toDate && typeof val.toDate === 'function') return val.toDate();
            return new Date(val);
        };

        const newDeadline = convertToDate(afterData.deadline);
        const oldDeadline = convertToDate(beforeData?.deadline);

        console.log(`Course ID: ${courseId}`);
        console.log(`New Deadline: ${newDeadline ? newDeadline.toISOString() : "MISSING"}`);
        console.log(`Old Deadline: ${oldDeadline ? oldDeadline.toISOString() : "EMPTY (New Document)"}`);

        // --------------------------------------------------------
        // 2. Detect new document OR deadline change
        // --------------------------------------------------------
        const isNewDocument = !beforeData;
        const hasDeadlineChanged = !oldDeadline || (newDeadline && oldDeadline && newDeadline.getTime() !== oldDeadline.getTime());

        if (isNewDocument || hasDeadlineChanged) {
            console.log(isNewDocument ? "Detected NEW document." : "Detected DEADLINE change.");

            // ----------------------------------------------------
            // Delete old task if exists
            // ----------------------------------------------------
            if (beforeData?.taskId) {
                console.log(`Removing old task: ${beforeData.taskId}`);
                try { await deleteTask(beforeData.taskId); } catch (e) { }
            }

            // ----------------------------------------------------
            // Create new task only if deadline is valid and future
            // ----------------------------------------------------
            if (newDeadline && newDeadline > new Date()) {
                try {
                    console.log("Creating Cloud Task...");
                    const taskId = await createTask(courseId, newDeadline, 1);
                    console.log(`Task created successfully: ${taskId}`);

                    // Update Firestore with new taskId
                    await afterSnapshot.ref.update({
                        taskId
                    });
                    console.log("Firestore document updated with taskId.");
                } catch (err) {
                    console.error("Error in task creation chain:", err);
                }
            } else {
                console.log("Skip: Deadline is missing or in the past.");
            }
        } else {
            console.log("Result: No functional change detected. Doing nothing.");
        }
    }
);