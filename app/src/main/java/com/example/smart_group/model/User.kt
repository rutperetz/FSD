package com.example.smart_group.model

data class User(
    val userName: String,
    val email: String,
    val passwordHash: String,
    val role: UserRole
)

enum class UserRole {
    STUDENT,
    ADMIN
}
