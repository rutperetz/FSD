package com.example.smart_group.ui.home

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smart_group.R

class HomeActivity : AppCompatActivity() {

    private val viewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val titleHome = findViewById<TextView>(R.id.title_home)
        val rvCourses = findViewById<RecyclerView>(R.id.rvCourses)
        val tvEmptyCourses = findViewById<TextView>(R.id.tvEmptyCourses)
        val progressBar = findViewById<ProgressBar>(R.id.progressBarHome)

        val adapter = HomeAdapter()

        rvCourses.layoutManager = LinearLayoutManager(this)
        rvCourses.adapter = adapter

        viewModel.screenTitle.observe(this) { title ->
            titleHome.text = title
        }

        viewModel.courses.observe(this) { courses ->
            adapter.submitList(courses)

            if (courses.isEmpty()) {
                rvCourses.visibility = View.GONE
                tvEmptyCourses.visibility = View.VISIBLE
            } else {
                rvCourses.visibility = View.VISIBLE
                tvEmptyCourses.visibility = View.GONE
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.toastMessage.observe(this) { message ->
            if (message != null) {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                viewModel.onToastShown()
            }
        }

        viewModel.loadCourses()
    }
}