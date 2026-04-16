package com.example.smart_group.ui.group

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.smart_group.R
import com.example.smart_group.data.repository.StudentRepository
import com.example.smart_group.ui.profile.ProfileActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class GroupActivity : AppCompatActivity() {

    private val viewModel: GroupProposalViewModel by viewModels()
    private lateinit var adapter: CandidateAdapter

    private lateinit var ivCourseImage: ImageView
    private lateinit var tvCourseTitle: TextView
    private lateinit var tvRoundInfo: TextView
    private lateinit var tvReasons: TextView
    private lateinit var rvCandidates: RecyclerView
    private lateinit var btnLike: Button
    private lateinit var btnDislike: Button
    private lateinit var btnBack: ImageView

    private lateinit var courseId: String
    private lateinit var currentStudentId: String

    private val studentRepository = StudentRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group)

        initViews()
        setupRecyclerView()
        setupClickListeners()
        observeData()
        setupBottomNav()

        courseId = intent.getStringExtra("courseId") ?: ""

        if (courseId.isBlank()) {
            Toast.makeText(this, "Missing courseId", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadCurrentStudentAndScreen()
    }

    private fun initViews() {
        tvCourseTitle = findViewById(R.id.tvCourseTitle)
        tvRoundInfo = findViewById(R.id.tvRoundInfo)
        ivCourseImage = findViewById(R.id.ivCourseImage)
        tvReasons = findViewById(R.id.tvReasons)
        rvCandidates = findViewById(R.id.rvCandidates)
        btnLike = findViewById(R.id.btnLike)
        btnDislike = findViewById(R.id.btnDislike)
        btnBack = findViewById(R.id.btnBack)

    }

    private fun setupRecyclerView() {
        adapter = CandidateAdapter(
            items = emptyList(),
            onDecline = { candidate ->
                viewModel.decline(courseId, currentStudentId, candidate)
            },
            onRemove = { candidate ->
                viewModel.removeCandidate(candidate)
            }
        )

        rvCandidates.layoutManager = LinearLayoutManager(this)
        rvCandidates.adapter = adapter
        rvCandidates.isNestedScrollingEnabled = false
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnLike.setOnClickListener {
            updateFeedbackSelection(liked = true)

            viewModel.submitFeedback(
                courseId = courseId,
                currentStudentId = currentStudentId,
                likedProposal = true,
                comment = ""
            )
        }

        btnDislike.setOnClickListener {
            updateFeedbackSelection(liked = false)

            viewModel.submitFeedback(
                courseId = courseId,
                currentStudentId = currentStudentId,
                likedProposal = false,
                comment = ""
            )
        }
    }
    private fun setupBottomNav() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)

        bottomNav.selectedItemId = R.id.nav_home

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true

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

    private fun updateFeedbackSelection(liked: Boolean) {
        if (liked) {
            btnLike.alpha = 1.0f
            btnDislike.alpha = 0.5f
        } else {
            btnLike.alpha = 0.5f
            btnDislike.alpha = 1.0f
        }
    }

    private fun observeData() {
        viewModel.course.observe(this) { course ->
            tvCourseTitle.text = course.title

            if (course.imageUrl.isNotBlank()) {
                Glide.with(this)
                    .load(course.imageUrl)
                    .into(ivCourseImage)
            }
        }


        viewModel.currentRoundText.observe(this) { roundText ->
            tvRoundInfo.text = roundText
        }

        viewModel.matchReasonsText.observe(this) { reasons ->
            tvReasons.text = reasons
        }

        viewModel.candidates.observe(this) { candidates ->
            adapter.updateData(candidates)
        }

        viewModel.message.observe(this) { message ->
            if (message.isNotBlank()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.feedbackSubmitted.observe(this) { submitted ->
            if (submitted == true) {
                Toast.makeText(
                    this,
                    "Feedback submitted successfully. Thank you!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun loadCurrentStudentAndScreen() {
        val firebaseUserId = FirebaseAuth.getInstance().currentUser?.uid

        if (firebaseUserId.isNullOrBlank()) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        lifecycleScope.launch {
            val student = studentRepository.getStudentByUserId(firebaseUserId)

            if (student == null) {
                Toast.makeText(
                    this@GroupActivity,
                    "Student not found for current user",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
                return@launch
            }

            currentStudentId = student.studentId
            android.util.Log.d("GROUP_DEBUG", "courseId = $courseId")
            android.util.Log.d("GROUP_DEBUG", "currentStudentId = $currentStudentId")
            viewModel.loadScreen(courseId, currentStudentId)
        }
    }
}