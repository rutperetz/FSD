package com.example.smart_group.data.model

data class User(
    val userId: String, //firebase uid
    val userName: String,
    val email: String,
    val role: UserRole
)

enum class UserRole {
    STUDENT,
    ADMIN
}
