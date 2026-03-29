package com.example.smart_group.ui.group

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
    private val onApprove: (CandidateUiModel) -> Unit,
    private val onDecline: (CandidateUiModel) -> Unit,
    private val onRemove: (CandidateUiModel) -> Unit
) : RecyclerView.Adapter<CandidateAdapter.CandidateViewHolder>() {

    inner class CandidateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvStudentName: TextView = itemView.findViewById(R.id.tvStudentName)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        private val tvActionHint: TextView = itemView.findViewById(R.id.tvActionHint)
        private val layoutApproveDecline: LinearLayout =
            itemView.findViewById(R.id.layoutApproveDecline)
        private val btnApprove: Button = itemView.findViewById(R.id.btnApprove)
        private val btnDecline: Button = itemView.findViewById(R.id.btnDecline)
        private val btnRemove: Button = itemView.findViewById(R.id.btnRemove)

        fun bind(item: CandidateUiModel) {
            tvStudentName.text = item.displayName

            val status = item.status.orEmpty().uppercase()

            when (status) {
                "APPROVED" -> {
                    tvStatus.text = "Approved"
                    tvStatus.setBackgroundResource(R.drawable.bg_status_approved)
                    tvStatus.setTextColor(
                        ContextCompat.getColor(itemView.context, android.R.color.white)
                    )

                    tvActionHint.text = "This student has already been approved."
                    layoutApproveDecline.visibility = View.GONE
                    btnRemove.visibility = View.GONE
                }

                "DECLINED", "REJECTED" -> {
                    tvStatus.text = "Declined"
                    tvStatus.setBackgroundResource(R.drawable.bg_status_rejected)
                    tvStatus.setTextColor(
                        ContextCompat.getColor(itemView.context, android.R.color.white)
                    )

                    tvActionHint.text = "This student was not approved."
                    layoutApproveDecline.visibility = View.GONE
                    btnRemove.visibility = View.VISIBLE

                    btnRemove.setOnClickListener { onRemove(item) }
                }

                else -> {
                    tvStatus.text = "Pending"
                    tvStatus.setBackgroundResource(R.drawable.bg_status_pending)
                    tvStatus.setTextColor(
                        ContextCompat.getColor(itemView.context, R.color.primary_blue)
                    )

                    tvActionHint.text = "Choose whether to approve or decline this student."
                    layoutApproveDecline.visibility = View.VISIBLE
                    btnRemove.visibility = View.GONE

                    btnApprove.setOnClickListener { onApprove(item) }
                    btnDecline.setOnClickListener { onDecline(item) }
                }
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