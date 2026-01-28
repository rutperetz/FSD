package com.example.smart_group.model

data class MatchGroup(
    val groupId: String = "",
    val roundId: String = "",
    val memberIds: List<String> = emptyList(),
    val groupScore: Double = 0.0,
    val groupStatus: Boolean = false,
    val groupReasons: GroupReasons = GroupReasons()
)