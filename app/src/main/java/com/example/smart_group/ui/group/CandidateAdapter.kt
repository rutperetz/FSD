package com.example.smart_group.ui.group

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smart_group.R
import com.example.smart_group.data.model.CandidateUiModel

class CandidateAdapter(
    private var items: List<CandidateUiModel>,
    private val onApprove: (CandidateUiModel) -> Unit,
    private val onDecline: (CandidateUiModel) -> Unit
) : RecyclerView.Adapter<CandidateAdapter.CandidateViewHolder>() {

    inner class CandidateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvStudentName: TextView = itemView.findViewById(R.id.tvStudentName)
        private val btnApprove: Button = itemView.findViewById(R.id.btnApprove)
        private val btnDecline: Button = itemView.findViewById(R.id.btnDecline)

        fun bind(item: CandidateUiModel) {
            tvStudentName.text = item.displayName

            if (item.status == "APPROVED") {
                btnApprove.visibility = View.GONE
                btnDecline.visibility = View.GONE
            } else {
                btnApprove.visibility = View.VISIBLE
                btnDecline.visibility = View.VISIBLE

                btnApprove.setOnClickListener { onApprove(item) }
                btnDecline.setOnClickListener { onDecline(item) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CandidateViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_candidate, parent, false)
        return CandidateViewHolder(view)
    }

    override fun onBindViewHolder(holder: CandidateViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<CandidateUiModel>) {
        items = newItems
        notifyDataSetChanged()
    }
}