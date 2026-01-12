package com.example.smart_group.model

data class Student(
    val userName: String,
    val email: String,
    val answers: Map<String, Any>,
    val normalizedAnswers: List<Int>
)

