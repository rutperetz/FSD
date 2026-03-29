package com.example.smart_group.data.model

data class Group(
    val groupId: String = "",
    val courseId: String = "",
    val memberIds: List<String> = emptyList(),
    var groupScore: Double = 0.0,
    val groupStatus: Boolean = true
)