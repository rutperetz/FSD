package com.example.smart_group.data.model

data class MatchRound(
    val roundId: String,//UUID
    val courseId:String,
    val roundNumber: Int,
    val groups: List<Group>,
    val unassignedStudents: List<String>,
    val status:RoundStatus
)
enum class RoundStatus {
    RUNNING,
    COMPLETED
}


