package com.example.smart_group.ui.group

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smart_group.data.model.CandidateUiModel
import com.example.smart_group.data.model.Course
import com.example.smart_group.data.repository.CourseRepository
import com.example.smart_group.data.repository.EnrollmentRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class GroupProposalViewModel : ViewModel() {

    private val courseRepository = CourseRepository()
    private val enrollmentRepository = EnrollmentRepository()
    private val db = FirebaseFirestore.getInstance()

    private val _course = MutableLiveData<Course>()
    val course: LiveData<Course> = _course

    private val _candidates = MutableLiveData<List<CandidateUiModel>>()
    val candidates: LiveData<List<CandidateUiModel>> = _candidates

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    private var currentCandidates: MutableList<CandidateUiModel> = mutableListOf()

    fun loadScreen(courseId: String, currentStudentId: String) {
        viewModelScope.launch {
            try {
                val course = courseRepository.getCourse(courseId)
                if (course != null) {
                    _course.value = course
                } else {
                    _message.value = "Course not found"
                }

                val enrollments = enrollmentRepository.getEnrollments(courseId)

                val filtered = enrollments
                    .filter { it.optIn }
                    .filter { it.studentId != currentStudentId }
                    .shuffled()
                    .take(3)

                currentCandidates = filtered.mapIndexed { index, enrollment ->
                    CandidateUiModel(
                        studentId = enrollment.studentId,
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

        currentCandidates = currentCandidates.filter { currentItem ->
            currentItem.studentId != candidate.studentId
        }.toMutableList()

        _candidates.value = currentCandidates.toList()
    }

    private fun saveDecision(
        courseId: String,
        currentStudentId: String,
        candidate: CandidateUiModel,
        decision: String
    ) {
        val data = hashMapOf(
            "candidateStudentId" to candidate.studentId,
            "decision" to decision,
            "shownAt" to System.currentTimeMillis()
        )

        db.collection("courses")
            .document(courseId)
            .collection("proposals")
            .document(currentStudentId)
            .collection("candidates")
            .document(candidate.studentId)
            .set(data)
            .addOnSuccessListener {
                _message.value = "${candidate.displayName} $decision"
            }
            .addOnFailureListener {
                _message.value = it.message ?: "Failed to save decision"
            }
    }
}