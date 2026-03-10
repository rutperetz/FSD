package com.example.smart_group.ui.courses

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smart_group.R
import com.example.smart_group.ui.profile.ProfileActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.widget.Toast

class CoursesActivity : ComponentActivity() {

    private lateinit var adapter: CourseAdapter

    private val allCourses = mutableListOf(
        CourseUiModel(
            id = 1,
            title = "FSD",
            description = "Full Stack Development",
            category = "שפות תכנות",
            imageRes = R.drawable.img_code,
            videoUrl = ""
        ),
        CourseUiModel(
            id = 2,
            title = "Algorithm",
            description = "Algorithms and problem solving",
            category = "מדעי המחשב",
            imageRes = R.drawable.img_cs,
            videoUrl = ""
        ),
        CourseUiModel(
            id = 3,
            title = "Java",
            description = "OOP and Collections",
            category = "שפות תכנות",
            imageRes = R.drawable.img_code,
            videoUrl = ""
        ),
        CourseUiModel(
            id = 4,
            title = "Linear Algebra",
            description = "Matrices and vectors",
            category = "מתמטיקה",
            imageRes = R.drawable.img_math,
            videoUrl = ""
        )
    )

    private var currentSearchQuery: String = ""

    private lateinit var rvCourses: RecyclerView
    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_courses)

        rvCourses = findViewById(R.id.rvCourses)
        bottomNav = findViewById(R.id.bottom_nav)

        bottomNav.selectedItemId = R.id.nav_home

        rvCourses.layoutManager = LinearLayoutManager(this)

        adapter = CourseAdapter(
            context = this,
            items = getFilteredCourses().toMutableList(),
            isAdmin = false,
            onDeleteClicked = { }
        )
        rvCourses.adapter = adapter

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


        refreshList()
    }

//    private fun showSearchDialog() {
//        val input = EditText(this)
//        input.hint = "Search course"
//
//        AlertDialog.Builder(this)
//            .setTitle("Search")
//            .setView(input)
//            .setPositiveButton("Search") { _, _ ->
//                currentSearchQuery = input.text.toString().trim()
//                refreshList()
//            }
//            .setNegativeButton("Cancel", null)
//            .show()
//    }

    private fun getFilteredCourses(): List<CourseUiModel> {
        return allCourses.filter { course ->
            course.title.contains(currentSearchQuery, ignoreCase = true)
        }
    }

    private fun refreshList() {
        adapter.updateData(getFilteredCourses())
    }
}