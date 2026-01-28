package com.example.smart_group.data

import com.example.smart_group.model.MatchRound
import com.example.smart_group.model.RejectReasons
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await

class GroupRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private fun roundRef(courseId: String, roundId: String) =
        db.collection("courses")
            .document(courseId)
            .collection("rounds")
            .document(roundId)

    // ✅ NEW: Realtime listener
    fun listenToRound(
        courseId: String,
        roundId: String,
        onUpdate: (MatchRound?) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration {
        return roundRef(courseId, roundId).addSnapshotListener { snapshot, e ->
            if (e != null) {
                onError(e)
                return@addSnapshotListener
            }
            onUpdate(snapshot?.toObject<MatchRound>())
        }
    }

    suspend fun getRound(courseId: String, roundId: String): MatchRound? {
        val snapshot = roundRef(courseId, roundId).get().await()
        return snapshot.toObject<MatchRound>()
    }

    suspend fun setGroupStatus(
        courseId: String,
        roundId: String,
        groupId: String,
        newStatus: Boolean
    ) {
        val ref = roundRef(courseId, roundId)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(ref)
            val round = snapshot.toObject<MatchRound>() ?: return@runTransaction

            val updatedGroups = round.matchGroups.map { group ->
                if (group.groupId == groupId) {
                    group.copy(groupStatus = newStatus)
                } else {
                    group
                }
            }

            transaction.update(ref, "matchGroups", updatedGroups)
        }.await()
    }

    suspend fun saveRejectReasons(
        courseId: String,
        roundId: String,
        groupId: String,
        reasons: RejectReasons
    ) {
        val ref = roundRef(courseId, roundId)
        val path = "rejectReasonsByGroup.$groupId"
        ref.update(path, reasons).await()
    }
}