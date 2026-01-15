package com.example.smart_group.data.model

data class Student(
    val studentId:String,//UUID
    val userId: String?,  //firebase uid
    val userName: String,
    val email: String,
    val answers: Answers,
    val normalizedAnswers: List<Int>
    /*gender_male
    gender_female
    pref_men
    pref_women
    pref_none
    avail_morning
    avail_afternoon
    avail_evening
    avail_weekend
    style_individual
    style_collaborative
    mode_oncampus
    mode_remote
    lang_hebrew
    lang_english
    lang_arabic
    task_fixed
    task_flexible*/
)

data class Answers(
    val gender: String,
    val genderPreference: String,
    val availability: List<String>,
    val workStyle: String,
    val workMode: List<String>,
    val language: String,
    val taskPreference: String
)
