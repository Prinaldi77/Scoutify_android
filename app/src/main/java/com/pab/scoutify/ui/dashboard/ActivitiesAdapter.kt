package com.pab.scoutify.ui.dashboard

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pab.scoutify.databinding.ItemActivityBinding
import com.pab.scoutify.model.ScoutActivity

class ActivitiesAdapter(
    private val activities: List<ScoutActivity>,
    private val isPembina: Boolean,
    private val onDeleteClick: (ScoutActivity) -> Unit
) : RecyclerView.Adapter<ActivitiesAdapter.ActivityViewHolder>() {

    class ActivityViewHolder(val binding: ItemActivityBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
        val binding = ItemActivityBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ActivityViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        val activity = activities[position]
        holder.binding.apply {
            tvActivityBadge.text = activity.badgeText
            tvActivityBadge.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor(activity.badgeColor))
            tvActivityCategory.text = activity.category
            tvActivityTitle.text = activity.title
            tvActivityDate.text = activity.date
            tvActivityTime.text = activity.time
            tvActivityLocation.text = activity.location

            if (isPembina) {
                btnDeleteActivity.visibility = android.view.View.VISIBLE
                btnDeleteActivity.setOnClickListener { onDeleteClick(activity) }
            } else {
                btnDeleteActivity.visibility = android.view.View.GONE
            }
        }
    }

    override fun getItemCount() = activities.size
}