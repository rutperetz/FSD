async function verifyRequest(req) {

    if (process.env.FUNCTIONS_EMULATOR === "true") {
        console.log("Skipping auth (emulator)");
        return;
    }

    const secret = req.headers["x-task-secret"];

    if (!secret || secret !== process.env.TASK_SECRET) {
        throw new Error("Unauthorized");
    }
}

module.exports = { verifyRequest };