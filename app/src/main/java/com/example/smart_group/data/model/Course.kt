package com.example.smart_group.data.model

data class Course(
    val courseId: String = "",
    val title: String = "",
    val lecturer: String = "",
    val groupSize: GroupSize = GroupSize(),
    val deadline: String = "",
    val groupingStatus: GroupingStatus = GroupingStatus.PENDING,
    val currentRound: Int = 0
)

data class GroupSize(
    val min: Int = 0,
    val max: Int = 0
)

enum class GroupingStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED
}