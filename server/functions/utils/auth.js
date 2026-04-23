const { OAuth2Client } = require("google-auth-library");
const { defineString } = require("firebase-functions/params");

// ------------------------------------------------------------
// OAuth2 client for verifying signed Cloud Tasks requests
// ------------------------------------------------------------
const client = new OAuth2Client();

// FUNCTION_URL is the expected audience for the OIDC token
const FUNCTION_URL = defineString("FUNCTION_URL");

// ------------------------------------------------------------
// verifyRequest(req)
// Validates that the incoming request is from a trusted
// Google Cloud Task (service account).
//
// Emulator mode:
//   - Skips authentication entirely
//
// Production mode:
//   - Requires Authorization: Bearer <token>
//   - Verifies token signature + audience
//   - Ensures the caller is a service account
// ------------------------------------------------------------
async function verifyRequest(req) {

    // Emulator → skip authentication
    if (process.env.FUNCTIONS_EMULATOR === "true") {
        console.log("Skipping auth (emulator)");
        return;
    }

    const authHeader = req.headers.authorization;

    if (!authHeader || !authHeader.startsWith("Bearer ")) {
        throw new Error("Missing Authorization header");
    }

    const token = authHeader.split("Bearer ")[1];

    // Verify token with Google
    const ticket = await client.verifyIdToken({
        idToken: token,
        audience: FUNCTION_URL.value()// must match Cloud Task target URL
    });

    const payload = ticket.getPayload();
    // Only allow service accounts to call this function
    if (!payload.email ||!payload.email.endsWith("@gserviceaccount.com")) {
        throw new Error("Unauthorized");
    }

    return payload;
}

module.exports = { verifyRequest };
