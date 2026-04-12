package com.example.smart_group.data.repository

import com.example.smart_group.data.model.MatchFeedback
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class MatchFeedbackRepository {

    private val db = FirebaseFirestore.getInstance()

    suspend fun saveFeedback(feedback: MatchFeedback): Result<Unit> {
        return try {
            val feedbackMap = mapOf(
                "likedProposal" to feedback.likedProposal,
                "comment" to feedback.comment,
                "submittedAt" to feedback.submittedAt,
                "feedbackRoundId" to feedback.roundId
            )

            db.collection("courses")
                .document(feedback.courseId)
                .collection("proposals")
                .document(feedback.studentId)
                .set(
                    mapOf("matchFeedback" to feedbackMap),
                    com.google.firebase.firestore.SetOptions.merge()
                )
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}