package com.example.smart_group.model

data class GroupReasons(
    val gender: String? = null,
    val availability: List<String> = emptyList(),
    val workMode: List<String> = emptyList(),
    val workStyle: String? = null,
    val language: String? = null,
    val taskPreference: String? = null
)