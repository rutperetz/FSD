package com.example.smart_group.ui.group

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.smart_group.R
import com.example.smart_group.data.model.CandidateUiModel

class CandidateAdapter(
    private var items: List<CandidateUiModel>,
    private val onDeclineToggle: (CandidateUiModel, Boolean) -> Unit
) : RecyclerView.Adapter<CandidateAdapter.CandidateViewHolder>() {

    private var isGroupFinalized: Boolean = false

    fun setGroupFinalized(value: Boolean) {
        isGroupFinalized = value
        notifyDataSetChanged()
    }

    inner class CandidateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvStudentName: TextView = itemView.findViewById(R.id.tvStudentName)
        private val tvStudentEmail: TextView = itemView.findViewById(R.id.tvStudentEmail)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        private val layoutDecline: LinearLayout = itemView.findViewById(R.id.layoutDecline)
        private val btnDecline: Button = itemView.findViewById(R.id.btnDecline)

        fun bind(item: CandidateUiModel) {
            tvStudentName.text = item.displayName
            tvStudentEmail.text = item.email

            val isDeclined = item.status.orEmpty().uppercase() in listOf("DECLINED", "REJECTED")

            if (isGroupFinalized) {
                tvStatus.visibility = View.GONE
            } else {
                tvStatus.visibility = View.VISIBLE
                tvStatus.text = if (isDeclined) "Declined" else "Pending"

                if (isDeclined) {
                    tvStatus.setBackgroundResource(R.drawable.bg_status_rejected)
                    tvStatus.setTextColor(
                        ContextCompat.getColor(itemView.context, android.R.color.white)
                    )

                    btnDecline.backgroundTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(itemView.context, android.R.color.holo_red_light)
                    )
                    btnDecline.setTextColor(
                        ContextCompat.getColor(itemView.context, android.R.color.white)
                    )
                } else {
                    tvStatus.setBackgroundResource(R.drawable.bg_status_pending)
                    tvStatus.setTextColor(
                        ContextCompat.getColor(itemView.context, R.color.primary_blue)
                    )

                    btnDecline.backgroundTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(itemView.context, R.color.lightBlue)
                    )
                    btnDecline.setTextColor(
                        ContextCompat.getColor(itemView.context, R.color.primary_blue)
                    )
                }
            }

            layoutDecline.visibility = if (isGroupFinalized) View.GONE else View.VISIBLE

            btnDecline.setOnClickListener {
                val newDeclinedState = !isDeclined
                onDeclineToggle(item, newDeclinedState)
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