package com.example.smart_group.data.repository

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    //login
    suspend fun login(email: String, password: String): String {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        return result.user?.uid ?: throw Exception("Missing user id")
    }
    fun getCurrentUserId(): String? = auth.currentUser?.uid
    fun getCurrentEmail(): String? = auth.currentUser?.email

    suspend fun updateEmail(newEmail: String) {
        val user = auth.currentUser ?: throw Exception("No logged-in user")
        user.verifyBeforeUpdateEmail(newEmail).await()
    }

    suspend fun updatePassword(newPassword: String) {
        val user = auth.currentUser ?: throw Exception("No logged-in user")
        user.updatePassword(newPassword).await()
    }

    fun logout() {
        auth.signOut()
    }

    suspend fun sendPasswordReset(email: String) {
        auth.sendPasswordResetEmail(email).await()
    }
}