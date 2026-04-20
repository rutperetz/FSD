const { CloudTasksClient } = require("@google-cloud/tasks");

const PROJECT = process.env.GCLOUD_PROJECT || "smartgroup-48a3d";
const LOCATION = "europe-west3";
const QUEUE = "runGrouping";

let client;

if (process.env.FUNCTIONS_EMULATOR === "true") {
    client = new CloudTasksClient({
        servicePath: "127.0.0.1",
        port: 9499,
        sslCreds: require("@grpc/grpc-js").credentials.createInsecure(),
    });
    console.log("🛠️ CloudTasksClient initialized for Emulator");
} else {
    client = new CloudTasksClient();
}

async function createTask(courseId, runTime, round) {
    const parent = client.queuePath(PROJECT, LOCATION, QUEUE);
    const url = process.env.FUNCTIONS_EMULATOR === "true"
        ? `http://127.0.0.1:5001/${PROJECT}/us-central1/runAlgorithm`
        : `https://${LOCATION}-${PROJECT}.cloudfunctions.net/runAlgorithm`;

    const taskId = `course-${courseId}-${Date.now()}`;
    const taskPath = client.taskPath(PROJECT, LOCATION, QUEUE, taskId);

    const task = {
        name: taskPath,
        httpRequest: {
            httpMethod: "POST",
            url: url,
            headers: { "Content-Type": "application/json" },
            body: Buffer.from(JSON.stringify({ courseId, round })).toString("base64"),
        },
        scheduleTime: {
            seconds: Math.floor(runTime.getTime() / 1000)
        }
    };

    if (process.env.FUNCTIONS_EMULATOR !== "true") {
        task.httpRequest.oidcToken = {
            serviceAccountEmail: `${PROJECT}@appspot.gserviceaccount.com`
        };
    }

    console.log(`📡 Attempting to create task: ${taskId}`);

    try {
        const [response] = await client.createTask({ parent, task });
        console.log(`✅ Task created successfully: ${response.name}`);
        return taskId;
    } catch (error) {
        // --- התיקון כאן ---
        if (process.env.FUNCTIONS_EMULATOR === "true") {
            console.warn(`⚠️ Emulator Connection Issue (ECONNRESET): ${error.message}`);
            console.log(`🧪 Returning taskId [${taskId}] anyway to allow Firestore update.`);
            return taskId; // אנחנו מחזירים את ה-ID למרות הכישלון הטכני
        }

        console.error("❌ Production Error:", error.message);
        throw error; // בפרודקשן אנחנו כן רוצים שזה ייכשל
    }
}

async function deleteTask(taskId) {
    if (!taskId || process.env.FUNCTIONS_EMULATOR === "true") return;
    try {
        const name = client.taskPath(PROJECT, LOCATION, QUEUE, taskId);
        await client.deleteTask({ name });
    } catch (error) {
        console.warn(`⚠️ Delete failed: ${error.message}`);
    }
}

module.exports = { createTask, deleteTask };