package com.example.smart_group.ui.courses

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smart_group.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

class CoursesActivity : ComponentActivity() {

    private lateinit var adapter: CourseAdapter

    private val allCourses = mutableListOf(
        CourseUiModel(
            id = 1,
            title = "C#",
            description = "תחביר, OOP והיכרות עם .NET",
            category = "שפות תכנות",
            imageRes = R.drawable.img_code,
            videoUrl = "https://www.youtube.com/watch?v=GhQdlIFylQ8"
        ),
        CourseUiModel(
            id = 2,
            title = "Java",
            description = "מחלקות, ירושה, OOP ו-Collections",
            category = "שפות תכנות",
            imageRes = R.drawable.img_code,
            videoUrl = "https://www.youtube.com/watch?v=eIrMbAQSU34"
        ),
        CourseUiModel(
            id = 3,
            title = "Python",
            description = "משתנים, פונקציות ורשימות",
            category = "שפות תכנות",
            imageRes = R.drawable.img_code,
            videoUrl = "https://www.youtube.com/watch?v=rfscVS0vtbw"
        ),
        CourseUiModel(
            id = 4,
            title = "אינפי 1",
            description = "גבולות, נגזרות ואינטגרלים בסיסיים",
            category = "מתמטיקה",
            imageRes = R.drawable.img_math,
            videoUrl = "https://www.youtube.com/watch?v=WUvTyaaNkzM"
        ),
        CourseUiModel(
            id = 5,
            title = "אלגברה לינארית",
            description = "וקטורים, מטריצות ומרחבים וקטוריים",
            category = "מתמטיקה",
            imageRes = R.drawable.img_math,
            videoUrl = "https://www.youtube.com/watch?v=ZK3O402wf1c"
        ),
        CourseUiModel(
            id = 6,
            title = "מבני נתונים",
            description = "רשימות, מחסנית, תור, עצים וטבלאות גיבוב",
            category = "מדעי המחשב",
            imageRes = R.drawable.img_cs,
            videoUrl = "https://www.youtube.com/watch?v=bum_19loj9A"
        )
    )

    private var selectedCategory: String = "All"
    private var visibleCount = 5
    private val pageSize = 5
    private val isAdmin = true

    private lateinit var tvRole: TextView
    private lateinit var etSearch: EditText
    private lateinit var spCategory: Spinner
    private lateinit var rvCourses: RecyclerView
    private lateinit var btnLoadMore: Button
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var btnProfile: Button
    private lateinit var btnGps: Button
    private lateinit var btnStats: Button
    private lateinit var btnLogout: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_courses)

        tvRole = findViewById(R.id.tvRole)
        etSearch = findViewById(R.id.etSearch)
        spCategory = findViewById(R.id.spCategory)
        rvCourses = findViewById(R.id.rvCourses)
        btnLoadMore = findViewById(R.id.btnLoadMore)
        fabAdd = findViewById(R.id.fabAdd)
        btnProfile = findViewById(R.id.btnProfile)
        btnGps = findViewById(R.id.btnGps)
        btnStats = findViewById(R.id.btnStats)
        btnLogout = findViewById(R.id.btnLogout)

        tvRole.text = if (isAdmin) "Admin" else "Student"

        rvCourses.layoutManager = LinearLayoutManager(this)
        adapter = CourseAdapter(
            context = this,
            items = getVisibleFilteredCourses().toMutableList(),
            isAdmin = isAdmin,
            onDeleteClicked = { course ->
                allCourses.remove(course)
                refreshList()
                Toast.makeText(this, "Course deleted", Toast.LENGTH_SHORT).show()
            }
        )
        rvCourses.adapter = adapter

        val categories = listOf("All", "מתמטיקה", "שפות תכנות", "מדעי המחשב")
        spCategory.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)

        spCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedCategory = categories[position]
                visibleCount = pageSize
                refreshList()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        etSearch.addTextChangedListener {
            visibleCount = pageSize
            refreshList()
        }

        btnLoadMore.setOnClickListener {
            visibleCount += pageSize
            refreshList()
        }

        fabAdd.visibility = if (isAdmin) View.VISIBLE else View.GONE
        fabAdd.setOnClickListener {
            Toast.makeText(this, "Add Course clicked", Toast.LENGTH_SHORT).show()
        }

        btnProfile.setOnClickListener {
            Toast.makeText(this, "Profile clicked", Toast.LENGTH_SHORT).show()
        }

        btnGps.setOnClickListener {
            Toast.makeText(this, "GPS clicked", Toast.LENGTH_SHORT).show()
        }

        btnStats.setOnClickListener {
            Toast.makeText(this, "Stats clicked", Toast.LENGTH_SHORT).show()
        }

        btnLogout.setOnClickListener {
            Toast.makeText(this, "Logout clicked", Toast.LENGTH_SHORT).show()
        }

        refreshList()
    }

    private fun getFilteredCourses(): List<CourseUiModel> {
        val query = etSearch.text.toString().trim()

        return allCourses.filter { course ->
            val matchesCategory = selectedCategory == "All" || course.category == selectedCategory
            val matchesSearch = course.title.contains(query, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    private fun getVisibleFilteredCourses(): List<CourseUiModel> {
        return getFilteredCourses().take(visibleCount)
    }

    private fun refreshList() {
        val visibleCourses = getVisibleFilteredCourses()
        val filteredCourses = getFilteredCourses()

        adapter.updateData(visibleCourses)

        btnLoadMore.visibility =
            if (visibleCourses.size < filteredCourses.size) View.VISIBLE else View.GONE
    }
}