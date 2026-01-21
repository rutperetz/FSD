const admin = require("firebase-admin");

// Render – משתמשים ב־Service Account דרך ENV
if (!admin.apps.length) {
  admin.initializeApp({
    credential: admin.credential.applicationDefault(),
  });
}

const db = admin.firestore();

module.exports = db;
