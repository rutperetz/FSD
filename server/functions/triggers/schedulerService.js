const { onSchedule } = require("firebase-functions/v2/scheduler");
const { createTask } = require("../tasks/taskService"); 

// ------------------------------------------------------------
// A scheduled function that runs once a day to check for courses
// with deadlines approaching in 30 days. It creates Cloud Tasks for those courses.
// ------------------------------------------------------------

exports.dailyDeadlineChecker = onSchedule("0 0 * * *", async (event) => {
    const now = new Date();
    const thirtyDaysFromNow = new Date(now.getTime() + 30 * 24 * 60 * 60 * 1000);

    console.log("Running daily check for upcoming deadlines...");

    try {
        const coursesDocs = await dbService.getCoursesNeedingTasks(thirtyDaysFromNow);
        if (coursesDocs.length === 0) {
            console.log("No courses require task creation today.");
            return;
        }

        // schedule tasks for each course that needs it
        for (const doc of coursesDocs) {
            const data = doc.data();
            if (!data.taskId) {
                console.log(`Course ${doc.id} entered 30-day window. Creating task...`);
                const deadline = data.deadline.toDate();
                const taskId = await createTask(doc.id, deadline, 1);
                await doc.ref.update({ taskId });
            }
        }
    } catch (error) {
        console.error("Daily scheduler error:", error);
    }
});