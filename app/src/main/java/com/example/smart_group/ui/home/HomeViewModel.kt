package com.example.smart_group.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smart_group.data.model.Course
import com.example.smart_group.data.model.UserRole
import com.example.smart_group.data.repository.AuthRepository
import com.example.smart_group.data.repository.CourseRepository
import com.example.smart_group.data.repository.EnrollmentRepository
import com.example.smart_group.data.repository.StudentRepository
import com.example.smart_group.data.repository.UserRepository
import kotlinx.coroutines.launch

class HomeViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val userRepository: UserRepository = UserRepository(),
    private val studentRepository: StudentRepository = StudentRepository(),
    private val courseRepository: CourseRepository = CourseRepository(),
    private val enrollmentRepository: EnrollmentRepository = EnrollmentRepository()
) : ViewModel() {

    private val _courses = MutableLiveData<List<Course>>(emptyList())
    val courses: LiveData<List<Course>> = _courses

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _toastMessage = MutableLiveData<String?>()
    val toastMessage: LiveData<String?> = _toastMessage

    private val _screenTitle = MutableLiveData("My Courses")
    val screenTitle: LiveData<String> = _screenTitle

    fun loadCourses() {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                val currentUserId = authRepository.getCurrentUserId()
                if (currentUserId.isNullOrEmpty()) {
                    _toastMessage.value = "No logged in user found"
                    _courses.value = emptyList()
                    return@launch
                }

                val user = userRepository.getUser(currentUserId)
                if (user == null) {
                    _toastMessage.value = "User data not found"
                    _courses.value = emptyList()
                    return@launch
                }

                when (user.role) {
                    UserRole.ADMIN -> {
                        _screenTitle.value = "All Courses"
                        _courses.value = courseRepository.getAllCourses()
                    }

                    UserRole.STUDENT -> {
                        _screenTitle.value = "My Courses"

                        val student = studentRepository.getStudentByUserId(currentUserId)
                        if (student == null) {
                            _toastMessage.value = "Student data not found"
                            _courses.value = emptyList()
                            return@launch
                        }

                        val enrollments = enrollmentRepository
                            .getEnrollmentsByStudentId(student.studentId)
                            .filter { it.optIn }

                        val courseIds = enrollments.map { it.courseId }.distinct()

                        val studentCourses = courseIds.mapNotNull { courseId ->
                            courseRepository.getCourse(courseId)
                        }

                        _courses.value = studentCourses
                    }
                }

            } catch (e: Exception) {
                _toastMessage.value = e.message ?: "Failed to load courses"
                _courses.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onToastShown() {
        _toastMessage.value = null
    }
}