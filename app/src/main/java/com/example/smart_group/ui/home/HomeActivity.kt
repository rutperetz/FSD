package com.example.smart_group.ui.home

import android.content.Intent
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
import com.example.smart_group.ui.profile.ProfileActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.smart_group.ui.coursedetails.CourseDetailsActivity
class HomeActivity : AppCompatActivity() {

    private val viewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val titleHome = findViewById<TextView>(R.id.title_home)
        val rvCourses = findViewById<RecyclerView>(R.id.rvCourses)
        val tvEmptyCourses = findViewById<TextView>(R.id.tvEmptyCourses)
        val progressBar = findViewById<ProgressBar>(R.id.progressBarHome)

        val adapter = HomeAdapter { course ->

            val intent = Intent(this, CourseDetailsActivity::class.java)
            intent.putExtra("courseId", course.courseId)
            startActivity(intent)
        }

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

        viewModel.loadCourses()
    }
}