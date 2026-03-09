package com.example.smart_group.ui.group

import android.os.Bundle
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smart_group.R

class GroupActivity : AppCompatActivity() {

    private val viewModel: GroupProposalViewModel by viewModels()
    private lateinit var adapter: CandidateAdapter

    private lateinit var tvCourseTitle: TextView
    private lateinit var rvCandidates: RecyclerView
    private lateinit var btnBack: ImageView

    private lateinit var courseId: String
    private lateinit var currentStudentId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group)

        courseId = intent.getStringExtra("courseId") ?: "C1"
        currentStudentId = intent.getStringExtra("studentId") ?: "S1"

        tvCourseTitle = findViewById(R.id.tvCourseTitle)
        rvCandidates = findViewById(R.id.rvCandidates)
        btnBack = findViewById(R.id.btnBack)

        adapter = CandidateAdapter(
            items = emptyList(),
            onApprove = { candidate ->
                viewModel.approve(courseId, currentStudentId, candidate)
            },
            onDecline = { candidate ->
                viewModel.decline(courseId, currentStudentId, candidate)
            }
        )

        rvCandidates.layoutManager = LinearLayoutManager(this)
        rvCandidates.adapter = adapter

        btnBack.setOnClickListener {
            finish()
        }

        observeData()
        viewModel.loadScreen(courseId, currentStudentId)
    }

    private fun observeData() {
        viewModel.course.observe(this) { course ->
            tvCourseTitle.text = course.title
        }

        viewModel.candidates.observe(this) { candidates ->
            adapter.updateData(candidates)
        }

        viewModel.message.observe(this) { message ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }
}