const { onDocumentWritten } = require("firebase-functions/v2/firestore");
const { createTask, deleteTask } = require("../tasks/taskService");

exports.handleCourseDeadlineChange = onDocumentWritten(
    "courses/{courseId}",
    async (event) => {
        console.log("--------------------------------------------------");
        console.log("Trigger execution started");

        const courseId = event.params.courseId;

        // שליפת הנתונים בזהירות
        const beforeSnapshot = event.data?.before;
        const afterSnapshot = event.data?.after;

        const beforeData = beforeSnapshot?.exists ? beforeSnapshot.data() : null;
        const afterData = afterSnapshot?.exists ? afterSnapshot.data() : null;

        // 1. אם המסמך נמחק (אין after)
        if (!afterData) {
            console.log(`Document ${courseId} deleted.`);
            if (beforeData?.taskId) {
                console.log(`Cleaning up task: ${beforeData.taskId}`);
                await deleteTask(beforeData.taskId);
            }
            return;
        }

        // פונקציית עזר להמרת טיפוסים ל-Date
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

        // 2. בדיקה: האם זה מסמך חדש או שהדדליין השתנה?
        const isNewDocument = !beforeData;
        const hasDeadlineChanged = !oldDeadline || (newDeadline && oldDeadline && newDeadline.getTime() !== oldDeadline.getTime());

        if (isNewDocument || hasDeadlineChanged) {
            console.log(isNewDocument ? "Detected NEW document." : "Detected DEADLINE change.");

            // מחיקת Task ישן אם קיים (במקרה של עדכון)
            if (beforeData?.taskId) {
                console.log(`Removing old task: ${beforeData.taskId}`);
                try { await deleteTask(beforeData.taskId); } catch (e) { }
            }

            // יצירת Task חדש
            if (newDeadline && newDeadline > new Date()) {
                try {
                    console.log("Creating Cloud Task...");
                    const taskId = await createTask(courseId, newDeadline, 1);
                    console.log(`Task created successfully: ${taskId}`);

                    // עדכון ה-taskId ב-Firestore
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