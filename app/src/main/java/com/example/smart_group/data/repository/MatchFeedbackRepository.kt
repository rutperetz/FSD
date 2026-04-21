package com.example.smart_group.data.repository

import com.google.firebase.firestore.FieldValue
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
}