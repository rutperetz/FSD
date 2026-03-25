package com.example.smart_group.ui.coursedetails
import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.util.UUID

import com.example.smart_group.data.model.Enrollment
import com.example.smart_group.data.repository.FirestoreRepository
class CourseDetailsViewModel : ViewModel() {

    private val repo = FirestoreRepository()

    val toastMessage = MutableLiveData<String>()
    val currentEnrollment = MutableLiveData<Enrollment?>()

    fun loadEnrollment(courseId: String, studentId: String) {
        viewModelScope.launch {
            val enrollment = repo.enrollmentRepository
                .getEnrollmentByStudent(courseId, studentId)

            currentEnrollment.value = enrollment
        }
    }

    fun signUp(courseId: String, studentId: String) {
        viewModelScope.launch {

            val enrollment = currentEnrollment.value

            if (enrollment?.optIn == true) {
                toastMessage.value = "You are already registered"
                return@launch
            }

            val newEnrollment = Enrollment(
                enrollmentId = UUID.randomUUID().toString(),
                courseId = courseId,
                studentId = studentId,
                optIn = true
            )

            repo.enrollmentRepository.addEnrollment(courseId, newEnrollment)

            currentEnrollment.value = newEnrollment
            toastMessage.value = "Registered successfully"
        }
    }

    fun cancelSignUp(courseId: String, studentId: String) {
        viewModelScope.launch {

            val enrollment = currentEnrollment.value

            if (enrollment?.optIn == false || enrollment == null) {
                toastMessage.value = "You are not registered"
                return@launch
            }

            val updated = enrollment.copy(optIn = false)

            repo.enrollmentRepository.updateEnrollment(courseId, updated)

            currentEnrollment.value = updated
            toastMessage.value = "Registration cancelled"
        }
    }
}