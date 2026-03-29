package com.example.smart_group.data.model

data class CandidateDecision(
    val courseId: String = "",
    val studentId: String = "",
    val candidateStudentId: String = "",
    val decision: String = "PENDING",
    val shownAt: Long = 0L
)