package com.example.smart_group.ui.group

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smart_group.data.model.CandidateDecision
import com.example.smart_group.data.model.CandidateUiModel
import com.example.smart_group.data.model.Course
import com.example.smart_group.data.model.MatchFeedback
import com.example.smart_group.data.repository.CandidateDecisionRepository
import com.example.smart_group.data.repository.CourseRepository
import com.example.smart_group.data.repository.MatchFeedbackRepository
import com.example.smart_group.data.repository.MatchRoundRepository
import kotlinx.coroutines.launch

class GroupProposalViewModel : ViewModel() {

    private val courseRepository = CourseRepository()
    private val matchRoundRepository = MatchRoundRepository()
    private val candidateDecisionRepository = CandidateDecisionRepository()
    private val matchFeedbackRepository = MatchFeedbackRepository()

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
                    _message.value = studentMatchResult.exceptionOrNull()?.message
                        ?: "Failed to load match data"
                    return@launch
                }

                val studentMatch = studentMatchResult.getOrNull()
                if (studentMatch == null) {
                    _message.value = "Match data not found"
                    return@launch
                }

                currentRoundId = studentMatch.roundId
                _currentRoundText.value = "Round ${studentMatch.roundNumber}"
                _matchReasonsText.value = formatReasons(studentMatch.groupReasons)

                currentCandidates = studentMatch.candidateIds.mapIndexed { index, candidateId ->
                    CandidateUiModel(
                        studentId = candidateId,
                        displayName = "student ${index + 1}",
                        status = "PENDING"
                    )
                }.toMutableList()

                _candidates.value = currentCandidates.toList()

            } catch (e: Exception) {
                _message.value = e.message ?: "Failed to load group screen"
            }
        }
    }

    fun approve(courseId: String, currentStudentId: String, candidate: CandidateUiModel) {
        saveDecision(courseId, currentStudentId, candidate, "APPROVED")

        currentCandidates = currentCandidates.map { currentItem ->
            if (currentItem.studentId == candidate.studentId) {
                currentItem.copy(status = "APPROVED")
            } else {
                currentItem
            }
        }.toMutableList()

        _candidates.value = currentCandidates.toList()
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
    }

    fun removeCandidate(candidate: CandidateUiModel) {
        currentCandidates = currentCandidates.filter { currentItem ->
            currentItem.studentId != candidate.studentId
        }.toMutableList()

        _candidates.value = currentCandidates.toList()
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

            val feedback = MatchFeedback(
                courseId = courseId,
                roundId = currentRoundId,
                studentId = currentStudentId,
                likedProposal = likedProposal,
                comment = comment.trim(),
                submittedAt = System.currentTimeMillis()
            )

            val result = matchFeedbackRepository.saveFeedback(feedback)

            if (result.isSuccess) {
                _message.value = "המשוב נשלח בהצלחה. תודה!"
                _feedbackSubmitted.value = true
            } else {
                _message.value = result.exceptionOrNull()?.message ?: "שמירת המשוב נכשלה"
                _feedbackSubmitted.value = false
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

    private fun formatReasons(groupReasons: Map<String, Any>): String {
        val lines = mutableListOf<String>()

        groupReasons["availability"]?.let {
            lines.add("זמינות משותפת: ${formatReasonValue(it)}")
        }
        groupReasons["gender"]?.let {
            lines.add("מגדר: ${formatReasonValue(it)}")
        }
        groupReasons["genderPreference"]?.let {
            lines.add("העדפת מגדר: ${formatReasonValue(it)}")
        }
        groupReasons["language"]?.let {
            lines.add("שפה: ${formatReasonValue(it)}")
        }
        groupReasons["taskPreference"]?.let {
            lines.add("העדפת משימה: ${formatReasonValue(it)}")
        }
        groupReasons["teamPreference"]?.let {
            lines.add("העדפת צוות: ${formatReasonValue(it)}")
        }
        groupReasons["workMode"]?.let {
            lines.add("אופן עבודה: ${formatReasonValue(it)}")
        }
        groupReasons["workStyle"]?.let {
            lines.add("סגנון עבודה: ${formatReasonValue(it)}")
        }

        return if (lines.isEmpty()) {
            "לא נמצאו סיבות להצעה"
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