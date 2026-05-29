package com.pab.scoutify.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.pab.scoutify.R
import com.pab.scoutify.databinding.ItemAttendanceBinding
import com.pab.scoutify.model.AttendanceSession

class AttendanceAdapter(
    private val sessions: List<AttendanceSession>,
    private val isPembina: Boolean,
    private val onDeleteClick: (AttendanceSession) -> Unit,
    private val onItemClick: (AttendanceSession) -> Unit
) : RecyclerView.Adapter<AttendanceAdapter.AttendanceViewHolder>() {

    class AttendanceViewHolder(val binding: ItemAttendanceBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttendanceViewHolder {
        val binding = ItemAttendanceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AttendanceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AttendanceViewHolder, position: Int) {
        val session = sessions[position]
        holder.binding.apply {
            tvSessionTitle.text = session.title
            tvSessionDate.text = session.date
            tvSessionCount.text = "${session.presentCount}/${session.totalCount}"
            progressIndicator.progress = session.progress

            if (session.isPresent) {
                root.setCardBackgroundColor(ContextCompat.getColor(root.context, R.color.surfaceBrown))
                tvSessionTitle.setTextColor(ContextCompat.getColor(root.context, android.R.color.white))
                tvSessionDate.setTextColor(ContextCompat.getColor(root.context, android.R.color.white))
                tvSessionCount.setTextColor(ContextCompat.getColor(root.context, android.R.color.white))
            } else {
                root.setCardBackgroundColor(ContextCompat.getColor(root.context, android.R.color.white))
                tvSessionTitle.setTextColor(ContextCompat.getColor(root.context, R.color.primaryDarkBrown))
                tvSessionDate.setTextColor(ContextCompat.getColor(root.context, R.color.grey_text))
                tvSessionCount.setTextColor(ContextCompat.getColor(root.context, R.color.primaryDarkBrown))
            }

            if (isPembina) {
                btnDeleteAttendance.visibility = android.view.View.VISIBLE
                btnDeleteAttendance.setOnClickListener {
                    onDeleteClick(session)
                }
            } else {
                btnDeleteAttendance.visibility = android.view.View.GONE
            }

            root.setOnClickListener {
                if (!session.isPresent) {
                    onItemClick(session)
                }
            }
        }
    }

    override fun getItemCount() = sessions.size
}
