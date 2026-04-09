package com.example.smart_group.ui.search

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
import java.util.Locale

class SearchViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val userRepository: UserRepository = UserRepository(),
    private val studentRepository: StudentRepository = StudentRepository(),
    private val courseRepository: CourseRepository = CourseRepository(),
    private val enrollmentRepository: EnrollmentRepository = EnrollmentRepository()
) : ViewModel() {

    private val allCourses = mutableListOf<Course>()

    private val _filteredCourses = MutableLiveData<List<Course>>(emptyList())
    val filteredCourses: LiveData<List<Course>> = _filteredCourses

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _toastMessage = MutableLiveData<String?>()
    val toastMessage: LiveData<String?> = _toastMessage

    fun loadCourses() {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                val currentUserId = authRepository.getCurrentUserId()
                if (currentUserId.isNullOrEmpty()) {
                    _toastMessage.value = "No logged in user found"
                    _filteredCourses.value = emptyList()
                    return@launch
                }

                val user = userRepository.getUser(currentUserId)
                if (user == null) {
                    _toastMessage.value = "User data not found"
                    _filteredCourses.value = emptyList()
                    return@launch
                }

                val coursesToShow = when (user.role) {
                    UserRole.ADMIN -> {
                        courseRepository.getAllCourses()
                    }

                    UserRole.STUDENT -> {
                        val student = studentRepository.getStudentByUserId(currentUserId)
                        if (student == null) {
                            _toastMessage.value = "Student data not found"
                            _filteredCourses.value = emptyList()
                            return@launch
                        }

                        val enrollments = enrollmentRepository
                            .getEnrollmentsByStudentId(student.studentId)
                            .filter { it.optIn }

                        val courseIds = enrollments.map { it.courseId }.distinct()
                        courseRepository.getCoursesByIds(courseIds)
                    }
                }

                allCourses.clear()
                allCourses.addAll(coursesToShow)

                _filteredCourses.value = emptyList()

            } catch (e: Exception) {
                _toastMessage.value = e.message ?: "Failed to load courses"
                _filteredCourses.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchCourses(query: String) {
        val cleanQuery = query.trim()

        if (cleanQuery.isEmpty()) {
            _filteredCourses.value = emptyList()
            return
        }

        val results = allCourses.filter { course ->
            course.title.lowercase(Locale.getDefault())
                .contains(cleanQuery.lowercase(Locale.getDefault()))
        }

        _filteredCourses.value = results
    }

    fun onToastShown() {
        _toastMessage.value = null
    }
}