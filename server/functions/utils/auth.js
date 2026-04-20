const { OAuth2Client } = require("google-auth-library");
const { defineString } = require("firebase-functions/params");

const client = new OAuth2Client();

const FUNCTION_URL = defineString("FUNCTION_URL");

async function verifyRequest(req) {

    // לאימות באימולטור
    if (process.env.FUNCTIONS_EMULATOR === "true") {
        console.log("Skipping auth (emulator)");
        return;
    }

    const authHeader = req.headers.authorization;

    if (!authHeader || !authHeader.startsWith("Bearer ")) {
        throw new Error("Missing Authorization header");
    }

    const token = authHeader.split("Bearer ")[1];

    const ticket = await client.verifyIdToken({
        idToken: token,
        audience: FUNCTION_URL.value()
    });

    const payload = ticket.getPayload();

    if (!payload.email.endsWith("@gserviceaccount.com")) {
        throw new Error("Unauthorized");
    }

    return payload;
}

module.exports = { verifyRequest };
