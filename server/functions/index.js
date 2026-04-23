// ------------------------------------------------------------
// Firebase Admin initialization
// Must be called exactly once per Cloud Functions instance.
// ------------------------------------------------------------
const admin = require("firebase-admin");
admin.initializeApp();

// ------------------------------------------------------------
// Export Cloud Functions
// ------------------------------------------------------------
const { handleCourseDeadlineChange } = require("./triggers/courseTrigger");
const { runAlgorithm } = require("./services/algorithmRunner");

// Firestore trigger: reacts to course deadline changes
exports.handleCourseDeadlineChange = handleCourseDeadlineChange;

// HTTPS function: runs a grouping round (invoked by Cloud Tasks)
exports.runAlgorithm = runAlgorithm;