package com.example.smart_group

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CoursesFragment : Fragment(R.layout.fragment_courses) {

    private lateinit var recycler: RecyclerView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recycler = view.findViewById(R.id.rvCourses)
        recycler.layoutManager = LinearLayoutManager(requireContext())

        // נתוני דמו זמניים
        val dummyCourses = listOf(
            CourseUi("FSD", "Full Stack Development", 3, "01/02/2026"),
            CourseUi("ALGO", "Algorithms", 4, "10/02/2026"),
            CourseUi("OS", "Operating Systems", 5, "15/02/2026")
        )

        recycler.adapter = CourseAdapter(dummyCourses) { course ->
            // בהמשך נוסיף ניווט למסך פרטי קורס
        }
    }
}
