package com.example.smart_group.ui.courses

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smart_group.R

class CourseAdapter(
    private val context: Context,
    private var items: MutableList<CourseUiModel>,
    private val isAdmin: Boolean,
    private val onDeleteClicked: (CourseUiModel) -> Unit = {},
    private val onCourseClick: (CourseUiModel) -> Unit = {}
) : RecyclerView.Adapter<CourseAdapter.CourseViewHolder>() {

    class CourseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgCourse: ImageView = itemView.findViewById(R.id.imgCourse)
        val tvCourseTitle: TextView = itemView.findViewById(R.id.tvCourseTitle)
        val tvCourseDesc: TextView = itemView.findViewById(R.id.tvCourseDesc)
        val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        val btnPlay: ImageButton = itemView.findViewById(R.id.btnPlay)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_course, parent, false)
        return CourseViewHolder(view)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        val item = items[position]

        holder.imgCourse.setImageResource(item.imageRes)
        holder.tvCourseTitle.text = item.title
        holder.tvCourseDesc.text = "Lecturer: ${item.lecturer} | Deadline: ${item.deadline}"
        holder.tvCategory.text =
            "Group: ${item.minGroupSize}-${item.maxGroupSize} | Status: ${item.groupingStatus}"

        holder.tvCourseDesc.visibility = View.VISIBLE
        holder.tvCategory.visibility = View.VISIBLE
        holder.btnPlay.visibility = View.GONE

        if (isAdmin) {
            holder.btnDelete.visibility = View.VISIBLE
            holder.btnDelete.setOnClickListener {
                onDeleteClicked(item)
            }
        } else {
            holder.btnDelete.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            onCourseClick(item)
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<CourseUiModel>) {
        items = newItems.toMutableList()
        notifyDataSetChanged()
    }
}