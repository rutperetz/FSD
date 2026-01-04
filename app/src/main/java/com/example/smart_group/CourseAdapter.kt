package com.example.smart_group

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fsd.R

class CourseAdapter(
    private val items: List<CourseUi>,
    private val onClick: (CourseUi) -> Unit
) : RecyclerView.Adapter<CourseAdapter.CourseViewHolder>() {

    inner class CourseViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val title = view.findViewById<TextView>(R.id.tvCourseTitle)
        private val subtitle = view.findViewById<TextView>(R.id.tvCourseSubtitle)

        fun bind(item: CourseUi) {
            title.text = item.title
            subtitle.text = "Group size: ${item.groupSize} • Deadline: ${item.deadline}"
            itemView.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.course, parent, false)

        return CourseViewHolder(view)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}
