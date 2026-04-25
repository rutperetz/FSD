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

    //register
    suspend fun register(email: String, password: String): String {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        return result.user?.uid ?: throw Exception("Missing user id")
    }
    fun getCurrentUserId(): String? = auth.currentUser?.uid

    fun getCurrentEmail(): String? = auth.currentUser?.email

    //edit profile
    suspend fun updateEmail(newEmail: String) {
        val user = auth.currentUser ?: throw Exception("No logged-in user")
        user.verifyBeforeUpdateEmail(newEmail).await()
    }

    //edit profile
    suspend fun updatePassword(newPassword: String) {
        val user = auth.currentUser ?: throw Exception("No logged-in user")
        user.updatePassword(newPassword).await()
    }

    suspend fun reloadCurrentUser() {
        val user = auth.currentUser ?: throw Exception("No logged-in user")
        user.reload().await()
    }

//    fun isCurrentUserEmailVerified(): Boolean {
//        return auth.currentUser?.isEmailVerified == true
//    }

    fun logout() {
        auth.signOut()
    }

    //forgot password
    suspend fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email).await()
    }
}