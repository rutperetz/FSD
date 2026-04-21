package com.example.smart_group.data.model

data class MatchRound(
    val roundId: String = "",
    val courseId: String = "",
    val roundNumber: Int = 0,
    val matchGroups: List<MatchGroup> = emptyList(),
    val matchUnassigned: List<String> = emptyList(),
    val matchWeights: Map<String, Double> = emptyMap(),
    val feedback: Map<String, StudentFeedback> = emptyMap()
)

data class MatchGroup(
    val memberIds: List<String> = emptyList(),
    val groupScore: Double = 0.0,
    val groupStatus: Boolean = false,
    val groupReasons: GroupReasons = GroupReasons()
)

data class GroupReasons(
    val gender: String? = null,
    val genderPreference: String? = null,
    val availability: List<String> = emptyList(),
    val workMode: List<String> = emptyList(),
    val workStyle: List<String> = emptyList(),
    val language: List<String> = emptyList(),
    val taskPreference: List<String> = emptyList()
)

data class StudentFeedback(
    val approveGroup: Boolean = true,
    val rejectStudents: List<String> = emptyList(),
    val rejectReasons: RejectReasons = RejectReasons()
)

data class RejectReasons(
    val availability: Int? = null,
    val workStyle: Int? = null,
    val workMode: Int? = null,
    val language: Int? = null,
    val taskPreference: Int? = null
)