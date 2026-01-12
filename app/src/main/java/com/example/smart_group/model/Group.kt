package com.example.smart_group.model

data class Group(
    val groupId: String,
    val groupMembers: MutableList<Student>,
    var groupScore: Double=0.0,
    val groupStatus: Boolean

)
