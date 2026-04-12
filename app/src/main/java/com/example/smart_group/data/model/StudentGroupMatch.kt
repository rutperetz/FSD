package com.example.smart_group.data.model

data class StudentGroupMatch(
    val roundId: String = "",
    val roundNumber: Int = 0,
    val candidateIds: List<String> = emptyList(),
    val groupReasons: Map<String, Any> = emptyMap()
)