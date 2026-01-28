package com.example.smart_group.model

data class RejectReasons(
    val availability: Boolean = false,
    val workStyle: Boolean = false,
    val workMode: Boolean = false,
    val language: Boolean = false,
    val taskPreference: Boolean = false
)