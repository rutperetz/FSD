package com.example.smart_group.data.repository

import com.example.smart_group.data.model.CandidateDecision
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class CandidateDecisionRepository {

    private val db = FirebaseFirestore.getInstance()

    private fun candidateRef(
        courseId: String,
        studentId: String,
        candidateStudentId: String
    ) = db.collection("courses")
        .document(courseId)
        .collection("proposals")
        .document(studentId)
        .collection("candidates")
        .document(candidateStudentId)

    suspend fun saveDecision(decision: CandidateDecision): Result<Unit> {
        return try {
            candidateRef(
                decision.courseId,
                decision.studentId,
                decision.candidateStudentId
            ).set(decision).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

//    suspend fun getDecisionsForStudent(
//        courseId: String,
//        studentId: String
//    ): Result<List<CandidateDecision>> {
//        return try {
//            val decisions = db.collection("courses")
//                .document(courseId)
//                .collection("proposals")
//                .document(studentId)
//                .collection("candidates")
//                .get()
//                .await()
//                .toObjects(CandidateDecision::class.java)
//
//            Result.success(decisions)
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//    }
}