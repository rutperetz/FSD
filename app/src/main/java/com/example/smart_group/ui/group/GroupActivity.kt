package com.example.smart_group.ui.group

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smart_group.R

class GroupActivity : AppCompatActivity() {

    private val viewModel: GroupProposalViewModel by viewModels()
    private lateinit var adapter: CandidateAdapter

    private lateinit var tvCourseTitle: TextView
    private lateinit var tvRoundInfo: TextView
    private lateinit var tvReasons: TextView
    private lateinit var rvCandidates: RecyclerView
    private lateinit var etFeedback: EditText
    private lateinit var btnLike: Button
    private lateinit var btnDislike: Button
    private lateinit var btnBack: ImageView
    private lateinit var tvFeedbackThanks: TextView

    private lateinit var courseId: String
    private lateinit var currentStudentId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group)

        courseId = intent.getStringExtra("courseId") ?: ""
        currentStudentId = intent.getStringExtra("studentId") ?: ""

        if (courseId.isBlank() || currentStudentId.isBlank()) {
            Toast.makeText(this, "Missing courseId or studentId", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initViews()
        setupRecyclerView()
        setupClickListeners()
        observeData()

        viewModel.loadScreen(courseId, currentStudentId)
    }

    private fun initViews() {
        tvCourseTitle = findViewById(R.id.tvCourseTitle)
        tvRoundInfo = findViewById(R.id.tvRoundInfo)
        tvReasons = findViewById(R.id.tvReasons)
        rvCandidates = findViewById(R.id.rvCandidates)
        etFeedback = findViewById(R.id.etFeedback)
        btnLike = findViewById(R.id.btnLike)
        btnDislike = findViewById(R.id.btnDislike)
        btnBack = findViewById(R.id.btnBack)
        tvFeedbackThanks = findViewById(R.id.tvFeedbackThanks)
    }

    private fun setupRecyclerView() {
        adapter = CandidateAdapter(
            items = emptyList(),
            onApprove = { candidate ->
                viewModel.approve(courseId, currentStudentId, candidate)
            },
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
            viewModel.submitFeedback(
                courseId = courseId,
                currentStudentId = currentStudentId,
                likedProposal = true,
                comment = etFeedback.text.toString().trim()
            )
        }

        btnDislike.setOnClickListener {
            viewModel.submitFeedback(
                courseId = courseId,
                currentStudentId = currentStudentId,
                likedProposal = false,
                comment = etFeedback.text.toString().trim()
            )
        }
    }

    private fun observeData() {
        viewModel.course.observe(this) { course ->
            tvCourseTitle.text = course.title
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
                etFeedback.visibility = View.GONE
                btnLike.visibility = View.GONE
                btnDislike.visibility = View.GONE
                tvFeedbackThanks.visibility = View.VISIBLE
            }
        }
    }
}