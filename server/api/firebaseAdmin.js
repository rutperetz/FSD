const admin = require("firebase-admin");
const serviceAccount = require("./smartgroup-48a3d-firebase-adminsdk-fbsvc-605cc91fde.json");

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
});

const db = admin.firestore();

module.exports = { admin, db };
