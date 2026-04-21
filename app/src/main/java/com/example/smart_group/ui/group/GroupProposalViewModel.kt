package com.example.smart_group.ui.group

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smart_group.data.model.CandidateDecision
import com.example.smart_group.data.model.CandidateUiModel
import com.example.smart_group.data.model.Course
import com.example.smart_group.data.model.GroupingStatus
import com.example.smart_group.data.repository.CandidateDecisionRepository
import com.example.smart_group.data.repository.CourseRepository
import com.example.smart_group.data.repository.MatchFeedbackRepository
import com.example.smart_group.data.repository.MatchRoundRepository
import com.example.smart_group.data.repository.StudentRepository
import kotlinx.coroutines.launch

class GroupProposalViewModel : ViewModel() {

    private val courseRepository = CourseRepository()
    private val matchRoundRepository = MatchRoundRepository()
    private val candidateDecisionRepository = CandidateDecisionRepository()
    private val matchFeedbackRepository = MatchFeedbackRepository()
    private val studentRepository = StudentRepository()

    private val _course = MutableLiveData<Course>()
    val course: LiveData<Course> = _course

    private val _candidates = MutableLiveData<List<CandidateUiModel>>()
    val candidates: LiveData<List<CandidateUiModel>> = _candidates

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    private val _matchReasonsText = MutableLiveData<String>()
    val matchReasonsText: LiveData<String> = _matchReasonsText

    private val _feedbackSubmitted = MutableLiveData<Boolean>()
    val feedbackSubmitted: LiveData<Boolean> = _feedbackSubmitted

    private val _currentRoundText = MutableLiveData<String>()
    val currentRoundText: LiveData<String> = _currentRoundText

    private val _noMatchMessage = MutableLiveData<String>()
    val noMatchMessage: LiveData<String> = _noMatchMessage

    private val _isGroupFinalized = MutableLiveData<Boolean>()
    val isGroupFinalized: LiveData<Boolean> = _isGroupFinalized

    private var currentCandidates: MutableList<CandidateUiModel> = mutableListOf()
    private var currentRoundId: String = ""

    fun loadScreen(courseId: String, currentStudentId: String) {
        viewModelScope.launch {
            try {
                _feedbackSubmitted.value = false

                val course = courseRepository.getCourse(courseId)
                if (course != null) {
                    _course.value = course
                } else {
                    _message.value = "Course not found"
                    return@launch
                }

                val studentMatchResult = matchRoundRepository.getStudentMatchForCurrentRound(
                    courseId = courseId,
                    studentId = currentStudentId
                )

                if (studentMatchResult.isFailure) {
                    val currentRound = course.currentRound

                    _currentRoundText.value = "Round $currentRound"
                    _candidates.value = emptyList()
                    _matchReasonsText.value = ""
                    _isGroupFinalized.value = false
                    _noMatchMessage.value = buildNoMatchMessage(course.groupingStatus)

                    return@launch
                }

                val studentMatch = studentMatchResult.getOrNull()

                if (studentMatch == null) {
                    val currentRound = course.currentRound

                    _currentRoundText.value = "Round $currentRound"
                    _candidates.value = emptyList()
                    _matchReasonsText.value = ""
                    _isGroupFinalized.value = false
                    _noMatchMessage.value = buildNoMatchMessage(course.groupingStatus)

                    return@launch
                }

                currentRoundId = studentMatch.roundId
                _noMatchMessage.value = ""
                _currentRoundText.value = "Round ${studentMatch.roundNumber}"
                _matchReasonsText.value = formatReasons(studentMatch.groupReasons)
                _isGroupFinalized.value = studentMatch.groupStatus

                val candidateList = mutableListOf<CandidateUiModel>()

                for ((index, candidateId) in studentMatch.candidateIds.withIndex()) {
                    val student = studentRepository.getStudent(candidateId)

                    val displayName = student?.userName?.takeIf { it.isNotBlank() }
                        ?: "Student ${index + 1}"

                    val email = student?.email?.takeIf { it.isNotBlank() }
                        ?: "No email"

                    candidateList.add(
                        CandidateUiModel(
                            studentId = candidateId,
                            displayName = displayName,
                            email = email,
                            status = "PENDING"
                        )
                    )
                }

                currentCandidates = candidateList.toMutableList()
                _candidates.value = currentCandidates.toList()

            } catch (e: Exception) {
                _message.value = e.message ?: "Failed to load group screen"
            }
        }
    }

    fun decline(courseId: String, currentStudentId: String, candidate: CandidateUiModel) {
        saveDecision(courseId, currentStudentId, candidate, "DECLINED")

        currentCandidates = currentCandidates.map { currentItem ->
            if (currentItem.studentId == candidate.studentId) {
                currentItem.copy(status = "DECLINED")
            } else {
                currentItem
            }
        }.toMutableList()

        _candidates.value = currentCandidates.toList()
        saveRejectedStudents(courseId, currentStudentId)
    }

    fun undoDecline(courseId: String, currentStudentId: String, candidate: CandidateUiModel) {
        saveDecision(courseId, currentStudentId, candidate, "PENDING")

        currentCandidates = currentCandidates.map { currentItem ->
            if (currentItem.studentId == candidate.studentId) {
                currentItem.copy(status = "PENDING")
            } else {
                currentItem
            }
        }.toMutableList()

        _candidates.value = currentCandidates.toList()
        saveRejectedStudents(courseId, currentStudentId)
    }

    fun submitFeedback(
        courseId: String,
        currentStudentId: String,
        likedProposal: Boolean,
        comment: String
    ) {
        viewModelScope.launch {
            if (currentRoundId.isBlank()) {
                _message.value = "Round ID is missing"
                _feedbackSubmitted.value = false
                return@launch
            }

            val result = matchFeedbackRepository.saveApproveGroup(
                courseId = courseId,
                roundId = currentRoundId,
                studentId = currentStudentId,
                approveGroup = likedProposal
            )

            if (result.isSuccess) {
                _message.value = ""
                _feedbackSubmitted.value = true
            } else {
                _message.value = result.exceptionOrNull()?.message ?: "Failed to save feedback"
                _feedbackSubmitted.value = false
            }
        }
    }

    private fun saveRejectedStudents(courseId: String, currentStudentId: String) {
        viewModelScope.launch {
            if (currentRoundId.isBlank()) {
                return@launch
            }

            val rejectedIds = currentCandidates
                .filter { it.status == "DECLINED" }
                .map { it.studentId }

            val result = matchFeedbackRepository.saveRejectedStudents(
                courseId = courseId,
                roundId = currentRoundId,
                studentId = currentStudentId,
                rejectStudents = rejectedIds
            )

            if (result.isFailure) {
                _message.value =
                    result.exceptionOrNull()?.message ?: "Failed to save rejected students"
            }
        }
    }

    private fun saveDecision(
        courseId: String,
        currentStudentId: String,
        candidate: CandidateUiModel,
        decision: String
    ) {
        viewModelScope.launch {
            val candidateDecision = CandidateDecision(
                courseId = courseId,
                studentId = currentStudentId,
                candidateStudentId = candidate.studentId,
                decision = decision,
                shownAt = System.currentTimeMillis()
            )

            val result = candidateDecisionRepository.saveDecision(candidateDecision)

            if (result.isSuccess) {
                _message.value = "${candidate.displayName} $decision"
            } else {
                _message.value = result.exceptionOrNull()?.message ?: "Failed to save decision"
            }
        }
    }

    private fun buildNoMatchMessage(groupingStatus: GroupingStatus): String {
        return when (groupingStatus) {
            GroupingStatus.COMPLETED -> {
                "No match has been found for you.\nPlease contact the lecturer."
            }
            GroupingStatus.IN_PROGRESS,
            GroupingStatus.PENDING -> {
                "No match has been found for you yet.\nPlease wait for the next round."
            }
        }
    }

    private fun formatReasons(groupReasons: Map<String, Any>): String {
        val lines = mutableListOf<String>()

        groupReasons["availability"]?.let {
            lines.add("Shared availability: ${formatReasonValue(it)}")
        }
        groupReasons["gender"]?.let {
            lines.add("Gender: ${formatReasonValue(it)}")
        }
        groupReasons["genderPreference"]?.let {
            lines.add("Gender preference: ${formatReasonValue(it)}")
        }
        groupReasons["language"]?.let {
            lines.add("Language: ${formatReasonValue(it)}")
        }
        groupReasons["taskPreference"]?.let {
            lines.add("Task preference: ${formatReasonValue(it)}")
        }
        groupReasons["teamPreference"]?.let {
            lines.add("Team preference: ${formatReasonValue(it)}")
        }
        groupReasons["workMode"]?.let {
            lines.add("Work mode: ${formatReasonValue(it)}")
        }
        groupReasons["workStyle"]?.let {
            lines.add("Work style: ${formatReasonValue(it)}")
        }

        return if (lines.isEmpty()) {
            "No match reasons found"
        } else {
            lines.joinToString("\n")
        }
    }

    private fun formatReasonValue(value: Any): String {
        return when (value) {
            is List<*> -> value.joinToString(", ")
            else -> value.toString()
        }
    }
}