const admin = require("firebase-admin");

function loadServiceAccount() {
  const raw = process.env.FIREBASE_SERVICE_ACCOUNT;
  if (!raw) {
    throw new Error("Missing FIREBASE_SERVICE_ACCOUNT env var");
  }

  // לפעמים Render/העתקה מחליפה שורות ב-\\n, אז מתקנים
  const fixed = raw.replace(/\\n/g, "\n");
  return JSON.parse(fixed);
}

if (!admin.apps.length) {
  const serviceAccount = loadServiceAccount();

  admin.initializeApp({
    credential: admin.credential.cert(serviceAccount),
    projectId: process.env.FIREBASE_PROJECT_ID || serviceAccount.project_id,
  });
}

const db = admin.firestore();
module.exports = db;
