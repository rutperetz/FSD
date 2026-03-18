package com.example.smart_group.data.model
import com.google.firebase.Timestamp

data class Course(
    val courseId: String,
    val title: String,
    val lecturer: String,
    val groupSize: GroupSize,
    val deadline: Timestamp? = null,
    val groupingStatus: GroupingStatus,
    val currentRound: Int
)

data class GroupSize(
    val min: Int,
    val max: Int
)

enum class GroupingStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED
}
