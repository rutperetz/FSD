package com.example.smart_group.data.repository

import com.example.smart_group.data.model.StudentFeedback
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class MatchFeedbackRepository {

    private val db = FirebaseFirestore.getInstance()

    suspend fun saveApproveGroup(
        courseId: String,
        roundId: String,
        studentId: String,
        approveGroup: Boolean
    ): Result<Unit> {
        return try {
            val updates = mapOf(
                "feedback.$studentId.approveGroup" to approveGroup
            )

            db.collection("courses")
                .document(courseId)
                .collection("matchRounds")
                .document(roundId)
                .update(updates)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveRejectedStudents(
        courseId: String,
        roundId: String,
        studentId: String,
        rejectStudents: List<String>
    ): Result<Unit> {
        return try {
            val updates = mapOf(
                "feedback.$studentId.rejectStudents" to rejectStudents
            )

            db.collection("courses")
                .document(courseId)
                .collection("matchRounds")
                .document(roundId)
                .update(updates)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    //FUTURE WORK
    suspend fun saveRejectReasons(
        courseId: String,
        roundId: String,
        studentId: String,
        rejectReasons: Map<String, Any>
    ): Result<Unit> {
        return try {
            val updates = mapOf(
                "feedback.$studentId.rejectReasons" to rejectReasons
            )

            db.collection("courses")
                .document(courseId)
                .collection("matchRounds")
                .document(roundId)
                .update(updates)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getStudentFeedback(
        courseId: String,
        roundId: String,
        studentId: String
    ): StudentFeedback {
        return try {
            val doc = db.collection("courses")
                .document(courseId)
                .collection("matchRounds")
                .document(roundId)
                .get()
                .await()

            val feedbackMap = doc.get("feedback") as? Map<*, *>
            val studentFeedbackMap = feedbackMap?.get(studentId) as? Map<*, *>

            StudentFeedback(
                approveGroup = studentFeedbackMap?.get("approveGroup") as? Boolean ?: true,
                rejectStudents = (studentFeedbackMap?.get("rejectStudents") as? List<*>)
                    ?.filterIsInstance<String>()
                    ?: emptyList(),
                rejectReasons = com.example.smart_group.data.model.RejectReasons()
            )

        } catch (e: Exception) {
            StudentFeedback()
        }
    }
}