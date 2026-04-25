package com.example.smart_group.data.repository

import com.example.smart_group.data.model.StudentGroupMatch
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class MatchRoundRepository {

    private val db = FirebaseFirestore.getInstance()

    suspend fun getStudentMatchForCurrentRound(
        courseId: String,
        studentId: String
    ): Result<StudentGroupMatch> {
        return try {
            val courseDoc = db.collection("courses")
                .document(courseId)
                .get()
                .await()

            if (!courseDoc.exists()) {
                return Result.failure(Exception("Course not found"))
            }

            val currentRound = (courseDoc.getLong("currentRound") ?: 1L).toInt()

            val snapshot = db.collection("courses")
                .document(courseId)
                .collection("matchRounds")
                .get()
                .await()

            if (snapshot.isEmpty) {
                return Result.failure(Exception("No match rounds found"))
            }

            val roundDoc = snapshot.documents.firstOrNull { doc ->
                (doc.getLong("roundNumber") ?: -1L).toInt() == currentRound
            } ?: return Result.failure(
                Exception("No match round found for currentRound = $currentRound")
            )

            val roundNumber = (roundDoc.getLong("roundNumber") ?: currentRound.toLong()).toInt()

            val matchGroups = roundDoc.get("matchGroups") as? List<*>
                ?: return Result.failure(Exception("No match groups found"))

            var candidateIds: List<String> = emptyList()
            var groupReasons: Map<String, Any> = emptyMap()
            var groupStatus = false

            for (group in matchGroups) {
                if (group is Map<*, *>) {
                    val memberIds = (group["memberIds"] as? List<*>)
                        ?.filterIsInstance<String>()
                        .orEmpty()

                    if (studentId in memberIds) {
                        candidateIds = memberIds.filter { it != studentId }

                        @Suppress("UNCHECKED_CAST")
                        groupReasons = group["groupReasons"] as? Map<String, Any> ?: emptyMap()

                        groupStatus = group["groupStatus"] as? Boolean ?: false
                        break
                    }
                }
            }

            if (candidateIds.isEmpty()) {
                return Result.failure(
                    Exception("Student is not assigned to any group in current round")
                )
            }

            Result.success(
                StudentGroupMatch(
                    roundId = roundDoc.id,
                    roundNumber = roundNumber,
                    candidateIds = candidateIds,
                    groupReasons = groupReasons,
                    groupStatus = groupStatus
                )
            )

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}