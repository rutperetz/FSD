const { CloudTasksClient } = require("@google-cloud/tasks");

const PROJECT = process.env.GCLOUD_PROJECT || "smartgroup-48a3d";
const LOCATION = process.env.TASKS_LOCATION;
const QUEUE = process.env.TASKS_QUEUE;
const FUNCTION_URL = process.env.FUNCTION_URL;
let client;
// ------------------------------------------------------------
// Cloud Tasks configuration
// Works both in Firebase Functions emulator and in production.
// ------------------------------------------------------------
function getTasksClient() {
    // ------------------------------------------------------------
    // Initialize CloudTasksClient
    // Emulator mode → connect to local task emulator
    // Production → use default credentials
    // ------------------------------------------------------------
    if (client) return client;
    if (process.env.FUNCTIONS_EMULATOR === "true") {
        
        client = new CloudTasksClient({
            servicePath: "127.0.0.1",
            port: 9499,
            sslCreds: require("@grpc/grpc-js").credentials.createInsecure(),
        });
        console.log("CloudTasksClient initialized for Emulator");
    } else {
        client = new CloudTasksClient();
    }
    return client;
}
// ------------------------------------------------------------
// Create a scheduled Cloud Task
// Schedules runAlgorithm(courseId, round) at a specific time.
// ------------------------------------------------------------
async function createTask(courseId, runTime, round) {
    const client = getTasksClient();

    if (!courseId || typeof round !== "number" || !(runTime instanceof Date)) {
        throw new Error("Invalid parameters for createTask()");
    }

    const parent = client.queuePath(PROJECT, LOCATION, QUEUE);

    // URL changes depending on emulator vs production
    const url = process.env.FUNCTIONS_EMULATOR === "true"
        ? `http://127.0.0.1:5001/${PROJECT}/europe-west3/runAlgorithm`
        : FUNCTION_URL;
    
    const taskId = `course-${courseId}-${Date.now()}`;
    const taskPath = client.taskPath(PROJECT, LOCATION, QUEUE, taskId);

    const task = {
        name: taskPath,
        httpRequest: {
            httpMethod: "POST",
            url: url,
            headers: {
                "Content-Type": "application/json",
                "x-task-secret": process.env.TASK_SECRET
             },
            body: Buffer.from(JSON.stringify({ courseId, round })).toString("base64"),
        },
        scheduleTime: {
            seconds: Math.floor(runTime.getTime() / 1000)
        }
    };

    // Production requires OIDC token for authenticated invocation
    if (process.env.FUNCTIONS_EMULATOR !== "true") {
        task.httpRequest.oidcToken = {
            serviceAccountEmail: `${PROJECT}@appspot.gserviceaccount.com`
        };
    }

    console.log(`Attempting to create task: ${taskId}`);

    try {
        const [response] = await client.createTask({ parent, task });
        console.log(` Task created successfully: ${response.name}`);
        return taskId;
    } catch (error) {
        // Emulator often throws ECONNRESET even when task is created successfully
        if (process.env.FUNCTIONS_EMULATOR === "true") {
            console.warn(`Emulator Connection Issue (ECONNRESET): ${error.message}`);
            return taskId;
        }
        
        console.error("Production Error:", error.message);
        throw error;
        
    }
}

// ------------------------------------------------------------
// Delete a Cloud Task (production only)
// ------------------------------------------------------------
async function deleteTask(taskId) {
    const client = getTasksClient();
    if (!taskId || process.env.FUNCTIONS_EMULATOR === "true") return;
    try {
        const name = client.taskPath(PROJECT, LOCATION, QUEUE, taskId);
        await client.deleteTask({ name });
    } catch (error) {
        console.warn(`Delete failed: ${error.message}`);
    }
}

module.exports = { createTask, deleteTask };