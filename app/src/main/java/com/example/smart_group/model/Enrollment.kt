package com.example.smart_group.model

data class Enrollment(
    val enrollmentId: String,//UUID
    val courseId: String,
    val studentId: String,
    val optIn: Boolean
)
