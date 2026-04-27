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
        const THIRTY_DAYS_MS = 30 * 24 * 60 * 60 * 1000;
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

            // --------------------------------------------------------
            // 3. If there was a previous task, delete it
            // --------------------------------------------------------
            if (beforeData?.taskId) {
                console.log(`Removing old task: ${beforeData.taskId}`);
            try { await deleteTask(beforeData.taskId); } catch (e) { }
            
                // Clear taskId in Firestore to prevent confusion
                await afterSnapshot.ref.update({ taskId: null });
            }

            const now = new Date();
            const isWithin30Days = newDeadline && (newDeadline.getTime() - now.getTime()) <= THIRTY_DAYS_MS;
            const isFuture = newDeadline && newDeadline > now;

            // --------------------------------------------------------
            // 4. If new deadline is within 30 days, create a new task immediately
            //    Otherwise, rely on the daily scheduler to pick it up later
            // --------------------------------------------------------
            if (isFuture && isWithin30Days) {
                try {
                    console.log("Deadline is within 30 days. Creating Cloud Task...");
                    const taskId = await createTask(courseId, newDeadline, 1);
                    await afterSnapshot.ref.update({ taskId });
                    console.log(`Task created and Firestore updated: ${taskId}`);
                } catch (err) {
                    console.error("Error creating task:", err);
                }
            } else if (isFuture && !isWithin30Days) {
                // מקרה: תזמון רחוק (חדש או שינוי לרחוק)
                console.log("Deadline is more than 30 days away. Scheduler will handle it later.");
            } else {
                console.log("Skip: Deadline is in the past.");
            }
        }
        
        else {
            console.log("Result: No functional change detected. Doing nothing.");
        }
    }
);