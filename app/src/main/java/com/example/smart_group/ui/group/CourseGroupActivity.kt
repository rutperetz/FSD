package com.example.smart_group.ui.group

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.smart_group.databinding.ActivityCourseGroupBinding
import com.example.smart_group.model.RejectReasons
import kotlinx.coroutines.launch

class CourseGroupActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCourseGroupBinding
    private val viewModel: CourseGroupViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCourseGroupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val courseId = intent.getStringExtra("courseId")
        val roundId = intent.getStringExtra("roundId")
        val groupId = intent.getStringExtra("groupId")

        if (courseId.isNullOrBlank() || roundId.isNullOrBlank() || groupId.isNullOrBlank()) {
            binding.status.text = "Missing courseId/roundId/groupId"
            return
        }

        viewModel.subscribe(courseId, roundId, groupId)

        lifecycleScope.launch {
            viewModel.state.collect { state ->
                binding.status.text = state.message

                state.group?.let { g ->
                    binding.title.text = "Group ${g.groupId} | Score ${g.groupScore}"
                    binding.status.text = if (g.groupStatus) "Accepted ✅" else "Pending ⏳"
                }
            }
        }

        binding.acceptBtn.setOnClickListener {
            viewModel.accept(courseId, roundId, groupId)
        }

        binding.rejectBtn.setOnClickListener {
            val reasons = RejectReasons(
                availability = true,
                workStyle = false,
                workMode = false,
                language = false,
                taskPreference = false
            )
            viewModel.reject(courseId, roundId, groupId, reasons)
        }
    }
}