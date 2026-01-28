package com.example.smart_group.model

data class MatchRound(
    val roundId: String = "",
    val courseId: String = "",
    val roundNumber: Int = 0,
    val matchGroups: List<MatchGroup> = emptyList(),
    val matchUnassigned: List<String> = emptyList(),
    val matchWeights: Map<String, Double> = emptyMap(),
    val feedback: Map<String, Any> = emptyMap()
)