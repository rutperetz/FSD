const admin = require("firebase-admin");

if (!process.env.FIREBASE_SERVICE_ACCOUNT_JSON) {
  throw new Error("Missing FIREBASE_SERVICE_ACCOUNT_JSON environment variable");
}

const serviceAccount = JSON.parse(
  process.env.FIREBASE_SERVICE_ACCOUNT_JSON
);

if (!admin.apps.length) {
  admin.initializeApp({
    credential: admin.credential.cert(serviceAccount),
  });
}

const db = admin.firestore();

module.exports = { db };
