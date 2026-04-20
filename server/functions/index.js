const admin = require("firebase-admin");
admin.initializeApp();

const { handleCourseDeadlineChange } = require("./triggers/courseTrigger");
const { runAlgorithm } = require("./services/algorithmRunner");

exports.handleCourseDeadlineChange = handleCourseDeadlineChange;
exports.runAlgorithm = runAlgorithm;