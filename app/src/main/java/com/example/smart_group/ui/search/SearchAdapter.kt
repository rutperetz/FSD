package com.example.smart_group.ui.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.smart_group.R
import com.example.smart_group.data.model.Course

class SearchAdapter(
    private val courses: MutableList<Course> = mutableListOf(),
    private val onCourseClick: ((Course) -> Unit)? = null
) : RecyclerView.Adapter<SearchAdapter.SearchViewHolder>() {

    inner class SearchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvCourseTitle: TextView = itemView.findViewById(R.id.tvCourseTitle)
        private val imgCourse: ImageView = itemView.findViewById(R.id.imgCourse)

        fun bind(course: Course) {
            tvCourseTitle.text = course.title

            Glide.with(itemView)
                .load(course.imageUrl)
                .into(imgCourse)

            itemView.setOnClickListener {
                onCourseClick?.invoke(course)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_course, parent, false)
        return SearchViewHolder(view)
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        holder.bind(courses[position])
    }

    override fun getItemCount(): Int = courses.size

    fun submitList(newCourses: List<Course>) {
        courses.clear()
        courses.addAll(newCourses)
        notifyDataSetChanged()
    }
}