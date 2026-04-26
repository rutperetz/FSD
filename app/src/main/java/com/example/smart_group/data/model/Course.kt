package com.example.smart_group.data.model

import com.google.firebase.Timestamp

data class Course(
    val courseId: String = "",
    val title: String = "",
    val lecturer: String = "",
    val groupSize: GroupSize = GroupSize(),
    val deadline: Timestamp? = null,
    val groupingStatus: GroupingStatus = GroupingStatus.PENDING,
    val currentRound: Int = 0,
    val imageUrl: String = "",
    val vectorsMap: Map<String, List<Int>> = emptyMap(),
    val taskId: String= "",
    val idToIndex: List<String> =emptyList()
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