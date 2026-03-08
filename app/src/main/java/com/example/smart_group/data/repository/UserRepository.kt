package com.example.smart_group.data.repository

import com.example.smart_group.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val usersRef = db.collection("users")

    suspend fun getUser(userId: String): User? =
        usersRef.document(userId).get().await().toObject(User::class.java)

    suspend fun getAllUsers(): List<User> =
        usersRef.get().await().toObjects(User::class.java)

    suspend fun addUser(user: User) {
        usersRef.document(user.userId).set(user).await()
    }

    suspend fun updateUser(user: User) {
        usersRef.document(user.userId).set(user).await()
    }

    suspend fun updateUserFields(
        userId: String,
        userName: String? = null,
        email: String? = null
    ) {
        val updates = mutableMapOf<String, Any>()

        if (userName != null) updates["userName"] = userName
        if (email != null) updates["email"] = email

        if (updates.isNotEmpty()) {
            usersRef.document(userId).update(updates).await()
        }
    }

    suspend fun deleteUser(userId: String) {
        usersRef.document(userId).delete().await()
    }
}