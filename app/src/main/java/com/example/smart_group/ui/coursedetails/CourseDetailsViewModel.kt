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
    val studentIdLiveData = MutableLiveData<String>()
    val isRegistered = MutableLiveData<Boolean>()
    val isLoading = MutableLiveData(false)
    val course = MutableLiveData<Course?>()
    val isDeadlinePassed = MutableLiveData<Boolean>()

    fun loadEnrollment(courseId: String, studentId: String) {
        viewModelScope.launch {

            try {
                val enrollment = repo.enrollmentRepository
                    .getEnrollmentByStudent(courseId, studentId)

                currentEnrollment.value = enrollment
                isRegistered.value = enrollment?.optIn == true

            } catch (e: Exception) {
                toastMessage.value = "Failed to load enrollment"
            }
        }
    }
    fun signUp(courseId: String) {
        viewModelScope.launch {

            if (isLoading.value == true) return@launch
            isLoading.value = true

            val enrollment = currentEnrollment.value

            if (enrollment == null) {
                toastMessage.value = "Enrollment not found"
                isLoading.value = false
                return@launch
            }

            if (enrollment.optIn) {
                toastMessage.value = "Already registered"
                isLoading.value = false
                return@launch
            }

            val updated = enrollment.copy(optIn = true)

            repo.enrollmentRepository.updateEnrollment(courseId, updated)

            currentEnrollment.value = updated
            isRegistered.value = true
            toastMessage.value = "Registered successfully"

            isLoading.value = false
        }
    }

    fun cancelSignUp(courseId: String) {
        viewModelScope.launch {

            if (isLoading.value == true) return@launch
            isLoading.value = true

            val enrollment = currentEnrollment.value

            if (enrollment == null) {
                toastMessage.value = "Enrollment not found"
                isLoading.value = false
                return@launch
            }

            if (!enrollment.optIn) {
                toastMessage.value = "You are not registered"
                isLoading.value = false
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

            if (data?.deadline != null) {
                val now = System.currentTimeMillis()
                val deadlineMillis = data.deadline.toDate().time

                isDeadlinePassed.value = deadlineMillis < now
            } else {
                isDeadlinePassed.value = false
            }
        }
    }

    fun updateDeadline(newTimestamp: com.google.firebase.Timestamp) {
        viewModelScope.launch {

            val current = course.value ?: return@launch

            val updated = current.copy(deadline = newTimestamp)

            repo.courseRepository.updateCourse(updated)

            course.value = updated
            toastMessage.value = "Deadline updated"
        }
    }

    fun updateGroupSize(courseId: String, min: Int, max: Int) {
        viewModelScope.launch {

            val current = course.value ?: return@launch

            val updated = current.copy(
                groupSize = com.example.smart_group.data.model.GroupSize(min, max)
            )

            repo.courseRepository.updateCourse(updated)

            course.value = updated
            toastMessage.value = "Group size updated"
        }
    }

    fun loadStudentAndData(userId: String, courseId: String) {
        viewModelScope.launch {
            try {
                val student = repo.studentRepository.getStudentByUserId(userId)

                if (student == null) {
                    toastMessage.value = "Student not found"
                    return@launch
                }

                studentIdLiveData.value = student.studentId

                // טעינת הכל במקום אחד
                loadEnrollment(courseId, student.studentId)
                //loadCourse(courseId)
                startListeningToCourse(courseId)

            } catch (e: Exception) {
                toastMessage.value = "Failed to load student"
            }
        }
    }

    fun startListeningToCourse(courseId: String) {
        repo.courseRepository.listenToCourse(courseId) { updatedCourse ->

            course.postValue(updatedCourse)

            // 🔥 חשוב מאוד לעדכן גם deadline
            if (updatedCourse.deadline != null) {
                val now = System.currentTimeMillis()
                val deadlineMillis = updatedCourse.deadline.toDate().time
                isDeadlinePassed.postValue(deadlineMillis < now)
            } else {
                isDeadlinePassed.postValue(false)
            }
        }
    }

}