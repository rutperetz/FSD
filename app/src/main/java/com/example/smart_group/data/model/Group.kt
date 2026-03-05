package com.example.smart_group.data.model

data class Group(
    val groupId: String,//UUID
    val memberIds: List<String>,
    var groupScore: Double=0.0,
    val groupStatus: Boolean
)
