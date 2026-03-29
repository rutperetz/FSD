package com.example.smart_group.ui.coursedetails
import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.util.UUID
import com.example.smart_group.data.model.Course
import com.example.smart_group.data.model.Enrollment
import com.example.smart_group.data.repository.FirestoreRepository
class CourseDetailsViewModel : ViewModel() {

    private val repo = FirestoreRepository()

    val toastMessage = MutableLiveData<String>()
    val currentEnrollment = MutableLiveData<Enrollment?>()
    val isRegistered = MutableLiveData<Boolean>()
    val isLoading = MutableLiveData(false)
    val course = MutableLiveData<Course?>()

    fun loadEnrollment(courseId: String, studentId: String) {
        viewModelScope.launch {
            val enrollment = repo.enrollmentRepository
                .getEnrollmentByStudent(courseId, studentId)

            currentEnrollment.value = enrollment

            isRegistered.value = enrollment?.optIn == true
        }
    }
    fun signUp(courseId: String, studentId: String) {
        viewModelScope.launch {

            if (isLoading.value == true) return@launch
            isLoading.value = true

            val enrollment = currentEnrollment.value

            if (enrollment?.optIn == true) {
                toastMessage.value = "Already registered"
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
            isRegistered.value = true
            toastMessage.value = "Registered successfully"
            isLoading.value = false
        }
    }

    fun cancelSignUp(courseId: String, studentId: String) {
        viewModelScope.launch {
            if (isLoading.value == true) return@launch
            isLoading.value = true

            val enrollment = currentEnrollment.value

            if (enrollment?.optIn != true) {
                toastMessage.value = "You are not registered"
                return@launch
            }

            val updated = enrollment.copy(optIn = false)

            repo.enrollmentRepository.updateEnrollment(courseId, updated)

            currentEnrollment.value = updated
            isRegistered.value = false
            toastMessage.value = "Registration cancelled"
            isLoading.value = false
        }
    }

    fun loadCourse(courseId: String) {
        viewModelScope.launch {
            val data = repo.courseRepository.getCourse(courseId)
            course.value = data
        }
    }
}