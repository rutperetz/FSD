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

            // ניגשים ל־proposal של הסטודנט
            val candidatesSnapshot = db.collection("courses")
                .document(courseId)
                .collection("proposals")
                .document(studentId)
                .collection("candidates")
                .get()
                .await()

            if (candidatesSnapshot.isEmpty) {
                return Result.failure(Exception("No candidates found"))
            }

            val candidateIds = candidatesSnapshot.documents.mapNotNull { doc ->
                doc.getString("candidateStudentId")
            }

            // אין לנו כרגע groupReasons בפיירבייס → נשים ריק
            val groupReasons = emptyMap<String, Any>()

            return Result.success(
                StudentGroupMatch(
                    roundId = "proposal_based",
                    roundNumber = 1, // זמני
                    candidateIds = candidateIds,
                    groupReasons = groupReasons
                )
            )

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    }