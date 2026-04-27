package com.example.smart_group.data.model

data class Student(
    val studentId: String = "",
    val userId: String = "",           // firebase uid
    val userName: String = "",
    val email: String = "",

    val answers: Answers = Answers(),


    val questionnaireCompleted: Boolean = false
)

data class Answers(
    val gender: String = "",
    val genderPreference: String = "",
    val availability: List<String> = emptyList(),
    val workStyle: List<String> = emptyList(),
    val workMode: List<String> = emptyList(),
    val language: List<String> = emptyList(),
    val taskPreference: List<String> = emptyList()
)
