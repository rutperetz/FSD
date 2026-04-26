// ------------------------------------------------------------
// Firebase Admin initialization
// Must be called exactly once per Cloud Functions instance.
// ------------------------------------------------------------
const admin = require("firebase-admin");
const { setGlobalOptions } = require("firebase-functions/v2");

setGlobalOptions({
    region: "europe-west3",
    memory: "512Mi"
});

if (admin.apps.length === 0) {
    admin.initializeApp();
}

const { handleCourseDeadlineChange } = require("./triggers/courseTrigger");
const { runAlgorithm } = require("./services/algorithmRunner");
// ------------------------------------------------------------
// Export Cloud Functions
// ------------------------------------------------------------

// Firestore trigger: reacts to course deadline changes
exports.handleCourseDeadlineChange = handleCourseDeadlineChange;

// HTTPS function: runs a grouping round (invoked by Cloud Tasks)
exports.runAlgorithm = runAlgorithm;