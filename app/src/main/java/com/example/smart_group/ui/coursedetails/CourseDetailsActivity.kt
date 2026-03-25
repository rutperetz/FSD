package com.example.smart_group.ui.coursedetails

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity

import com.example.smart_group.R
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth

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

        setupObservers()
        setupClicks()
    }

    private fun setupObservers() {
        vm.toastMessage.observe(this) {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
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
}