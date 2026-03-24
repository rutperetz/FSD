package com.example.smart_group.ui.courses

data class CourseUiModel(
    val courseId: String,
    val title: String,
    val lecturer: String,
    val minGroupSize: Int,
    val maxGroupSize: Int,
    val deadline: String,
    val groupingStatus: String,
    val currentRound: Int,
    val imageRes: Int
)