package com.example.smart_group.ui.coursedetails

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
//import com.example.smart_group.data.model.Course
import com.example.smart_group.R
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.bumptech.glide.Glide
import androidx.lifecycle.lifecycleScope
import com.example.smart_group.ui.profile.ProfileActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

class CourseDetailsActivity : AppCompatActivity() {

    private val vm: CourseDetailsViewModel by viewModels()

    private var courseId: String = ""
    private var studentId: String = ""

    // 🔹 כל ה־Views
    private lateinit var signUpButton: MaterialButton
    private lateinit var cancelButton: MaterialButton
    private lateinit var viewGroupText: TextView
    private lateinit var backArrow: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_course_details)

        // 🔥 חיבור ל־XML עם findViewById
        signUpButton = findViewById(R.id.signUpButton)
        cancelButton = findViewById(R.id.cancelButton)
        viewGroupText = findViewById(R.id.viewGroupText)
        backArrow = findViewById(R.id.back_arrow)

        // קבלת נתונים
        courseId = intent.getStringExtra("courseId") ?: ""
        studentId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        vm.loadEnrollment(courseId, studentId)
        vm.loadCourse(courseId)

       // loadStudentAndContinue()
        setupObservers()
        setupClicks()

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)

        bottomNav.selectedItemId = R.id.nav_home

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {

                R.id.nav_home -> {
                    true
                }

                R.id.nav_search -> {
                    Toast.makeText(this, "Search screen not implemented yet", Toast.LENGTH_SHORT).show()
                    true
                }

                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish()
                    true
                }

                else -> false
            }
        }

    }

    private fun setupObservers() {

        vm.toastMessage.observe(this) {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }

        vm.isLoading.observe(this) { loading ->
            signUpButton.isEnabled = !loading
            cancelButton.isEnabled = !loading
        }

        vm.isRegistered.observe(this) { isRegistered ->

            if (isRegistered) {
                signUpButton.isEnabled = false
                cancelButton.isEnabled = true
            } else {
                signUpButton.isEnabled = true
                cancelButton.isEnabled = false
            }
        }
        vm.course.observe(this) { course ->

            if (course != null) {

                findViewById<TextView>(R.id.courseTitle).text = course.title

                findViewById<TextView>(R.id.deadlineText).text =
                    course.deadline?.toDate()?.toString() ?: "No deadline"

                findViewById<TextView>(R.id.groupSizeText).text =
                    "● ${course.groupSize.min}-${course.groupSize.max} students"

                val image = findViewById<ImageView>(R.id.courseBanner)

                if (course.imageUrl.isNotEmpty()) {
                    Glide.with(this)
                        .load(course.imageUrl)
                        .into(image)
                }
            }
        }
    }

    private fun setupClicks() {

        signUpButton.setOnClickListener {
            vm.signUp(courseId, studentId)
        }

        cancelButton.setOnClickListener {
            vm.cancelSignUp(courseId, studentId)
        }

        viewGroupText.setOnClickListener {
            Toast.makeText(this, "Group screen coming soon", Toast.LENGTH_SHORT).show()
        }

        backArrow.setOnClickListener {
            finish()
        }
    }

//    private fun loadStudentAndContinue() {
//
//        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
//
//        val repo = com.example.smart_group.data.repository.StudentRepository()
//
//        lifecycleScope.launch {
//
//            val student = repo.getStudentByUserId(userId)
//
//            if (student == null) {
//                Toast.makeText(this@CourseDetailsActivity, "Student not found", Toast.LENGTH_SHORT).show()
//                return@launch
//            }
//
//            studentId = student.studentId
//
//            vm.loadEnrollment(courseId, studentId)
//            vm.loadCourse(courseId)
//        }
//    }


}