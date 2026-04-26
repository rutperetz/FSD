package com.example.smart_group.ui.search

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smart_group.R
import com.example.smart_group.ui.home.HomeActivity
import com.example.smart_group.ui.profile.ProfileActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.smart_group.ui.coursedetails.CourseDetailsActivity



class SearchActivity : AppCompatActivity() {

    private val viewModel: SearchViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        val etSearch = findViewById<EditText>(R.id.etSearchCourse)
        val rvSearchCourses = findViewById<RecyclerView>(R.id.rvSearchCourses)
        val tvEmptySearch = findViewById<TextView>(R.id.tvEmptySearch)
        val progressBar = findViewById<ProgressBar>(R.id.progressBarSearch)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)

//        val adapter = SearchAdapter()
        val adapter = SearchAdapter { course ->
            val intent = Intent(this, CourseDetailsActivity::class.java)
            intent.putExtra("courseId", course.courseId)
            startActivity(intent)
        }

        rvSearchCourses.layoutManager = LinearLayoutManager(this)
        rvSearchCourses.adapter = adapter

        viewModel.filteredCourses.observe(this) { courses ->
            adapter.submitList(courses)

            val query = etSearch.text.toString().trim()

            when {
                query.isEmpty() -> {
                    rvSearchCourses.visibility = View.GONE
                    tvEmptySearch.visibility = View.GONE
                }

                courses.isEmpty() -> {
                    rvSearchCourses.visibility = View.GONE
                    tvEmptySearch.visibility = View.VISIBLE
                }

                else -> {
                    rvSearchCourses.visibility = View.VISIBLE
                    tvEmptySearch.visibility = View.GONE
                }
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

        etSearch.addTextChangedListener { editable ->
            viewModel.searchCourses(editable?.toString().orEmpty())
        }

        bottomNav.selectedItemId = R.id.nav_search

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                    true
                }

                R.id.nav_search -> {
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