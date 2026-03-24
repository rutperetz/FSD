package com.example.smart_group.ui.courses

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smart_group.R
import com.example.smart_group.data.model.Course
import com.example.smart_group.data.model.Enrollment
import com.example.smart_group.data.repository.AuthRepository
import com.example.smart_group.data.repository.CourseRepository
import com.example.smart_group.data.repository.EnrollmentRepository
import com.example.smart_group.data.repository.StudentRepository
import com.example.smart_group.ui.profile.ProfileActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import java.util.UUID

class CoursesActivity : ComponentActivity() {

    private lateinit var adapter: CourseAdapter
    private lateinit var rvCourses: RecyclerView
    private lateinit var bottomNav: BottomNavigationView

    private val authRepository = AuthRepository()
    private val studentRepository = StudentRepository()
    private val enrollmentRepository = EnrollmentRepository()
    private val courseRepository = CourseRepository()

    private val allCourses = mutableListOf<CourseUiModel>()
    private var currentSearchQuery: String = ""

    private var currentStudentId: String? = null
    private var currentEnrollments: List<Enrollment> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_courses)

        rvCourses = findViewById(R.id.rvCourses)
        bottomNav = findViewById(R.id.bottom_nav)

        rvCourses.layoutManager = LinearLayoutManager(this)

        adapter = CourseAdapter(
            context = this,
            items = mutableListOf(),
            isAdmin = false,
            onDeleteClicked = {},
            onCourseClick = { course ->
                handleEnrollmentClick(course)
            }
        )

        rvCourses.adapter = adapter

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    currentSearchQuery = ""
                    refreshList()
                    true
                }

                R.id.nav_search -> {
                    showSearchDialog()
                    true
                }

                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }

                else -> false
            }
        }

        loadStudentCourses()
    }

    private fun loadStudentCourses() {
        lifecycleScope.launch {
            try {
                val userId = authRepository.getCurrentUserId()
                if (userId == null) {
                    Toast.makeText(
                        this@CoursesActivity,
                        "No logged in user found",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@launch
                }

                val student = studentRepository.getStudentByUserId(userId)
                if (student == null) {
                    Toast.makeText(
                        this@CoursesActivity,
                        "Student not found",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@launch
                }

                currentStudentId = student.studentId
                currentEnrollments = enrollmentRepository.getEnrollmentsByStudentId(student.studentId)

                val courseIds = currentEnrollments.map { it.courseId }.distinct()

                val firebaseCourses = mutableListOf<Course>()
                for (courseId in courseIds) {
                    val course = courseRepository.getCourse(courseId)
                    if (course != null) {
                        firebaseCourses.add(course)
                    }
                }

                allCourses.clear()
                allCourses.addAll(firebaseCourses.map { course -> mapCourseToUi(course) })

                refreshList()

            } catch (e: Exception) {
                Toast.makeText(
                    this@CoursesActivity,
                    "Failed to load courses: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun handleEnrollmentClick(course: CourseUiModel) {
        val studentId = currentStudentId
        if (studentId == null) {
            Toast.makeText(this, "Student not loaded yet", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val existingEnrollment = currentEnrollments.firstOrNull {
                    it.courseId == course.courseId && it.studentId == studentId
                }

                if (existingEnrollment != null) {
                    enrollmentRepository.deleteEnrollment(
                        courseId = course.courseId,
                        enrollmentId = existingEnrollment.enrollmentId
                    )

                    Toast.makeText(
                        this@CoursesActivity,
                        "Enrollment cancelled for ${course.title}",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val newEnrollment = Enrollment(
                        enrollmentId = UUID.randomUUID().toString(),
                        courseId = course.courseId,
                        studentId = studentId,
                        optIn = true
                    )

                    enrollmentRepository.addEnrollment(course.courseId, newEnrollment)

                    Toast.makeText(
                        this@CoursesActivity,
                        "Enrolled to ${course.title}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                currentEnrollments = enrollmentRepository.getEnrollmentsByStudentId(studentId)

            } catch (e: Exception) {
                Toast.makeText(
                    this@CoursesActivity,
                    "Enrollment action failed: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun mapCourseToUi(course: Course): CourseUiModel {
        return CourseUiModel(
            courseId = course.courseId,
            title = course.title,
            lecturer = course.lecturer,
            minGroupSize = course.groupSize.min,
            maxGroupSize = course.groupSize.max,
            deadline = course.deadline,
            groupingStatus = course.groupingStatus.name,
            currentRound = course.currentRound,
            imageRes = getImageForCourse(course.title)
        )
    }

    private fun getImageForCourse(title: String): Int {
        return when {
            title.contains("java", ignoreCase = true) -> R.drawable.img_code
            title.contains("algorithm", ignoreCase = true) -> R.drawable.img_cs
            title.contains("algebra", ignoreCase = true) -> R.drawable.img_math
            title.contains("fsd", ignoreCase = true) -> R.drawable.img_code
            else -> R.drawable.img_code
        }
    }

    private fun showSearchDialog() {
        val input = EditText(this)
        input.hint = "Search course"

        AlertDialog.Builder(this)
            .setTitle("Search")
            .setView(input)
            .setPositiveButton("Search") { _, _ ->
                currentSearchQuery = input.text.toString().trim()
                refreshList()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun getFilteredCourses(): List<CourseUiModel> {
        return allCourses.filter { course ->
            course.title.contains(currentSearchQuery, ignoreCase = true) ||
                    course.lecturer.contains(currentSearchQuery, ignoreCase = true)
        }
    }

    private fun refreshList() {
        adapter.updateData(getFilteredCourses())
    }
}