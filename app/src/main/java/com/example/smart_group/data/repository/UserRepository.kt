package com.example.smart_group.data.repository


import com.google.firebase.firestore.FirebaseFirestore
import com.example.smart_group.data.model.User
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

    suspend fun deleteUser(userId: String) {
        usersRef.document(userId).delete().await()
    }
}
