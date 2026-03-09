package com.example.smart_group.ui.courses

import android.content.Context
import android.content.Intent
import android.net.Uri
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
    private val onDeleteClicked: (CourseUiModel) -> Unit
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
        holder.tvCourseDesc.text = item.description
        holder.tvCategory.text = item.category

        holder.btnPlay.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.videoUrl))
            context.startActivity(intent)
        }

        if (isAdmin) {
            holder.btnDelete.visibility = View.VISIBLE
            holder.btnDelete.setOnClickListener {
                onDeleteClicked(item)
            }
        } else {
            holder.btnDelete.visibility = View.GONE
            holder.btnDelete.setOnClickListener(null)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun updateData(newItems: List<CourseUiModel>) {
        items = newItems.toMutableList()
        notifyDataSetChanged()
    }
}