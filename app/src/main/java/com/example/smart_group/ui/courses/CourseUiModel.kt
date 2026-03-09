package com.example.smart_group.ui.courses

data class CourseUiModel(
    val id: Int,
    val title: String,
    val description: String,
    val category: String,
    val imageRes: Int,
    val videoUrl: String
)