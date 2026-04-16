package com.example.smart_group.data.model

data class CandidateUiModel(
    val studentId: String = "",
    val displayName: String = "",
    val email: String,
    var status: String = "PENDING"
)