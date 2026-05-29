package com.pab.scoutify.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pab.scoutify.R
import com.pab.scoutify.databinding.ItemNotificationBinding
import com.pab.scoutify.model.Notification
import com.pab.scoutify.model.NotificationType

class NotificationAdapter(private val notifications: List<Notification>) :
    RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    class NotificationViewHolder(val binding: ItemNotificationBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding = ItemNotificationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NotificationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notifications[position]
        holder.binding.apply {
            tvNotifTitle.text = notification.title
            tvNotifDescription.text = notification.description
            tvNotifTime.text = notification.time

            val context = root.context
            when (notification.type) {
                NotificationType.WARNING -> {
                    iconContainer.setCardBackgroundColor(context.getColor(R.color.accentOrange))
                    ivNotifIcon.setImageResource(R.drawable.ic_warning)
                }
                NotificationType.INFO -> {
                    iconContainer.setCardBackgroundColor(context.getColor(R.color.badgeBlue))
                    ivNotifIcon.setImageResource(R.drawable.ic_info)
                }
                NotificationType.UPDATE -> {
                    iconContainer.setCardBackgroundColor(context.getColor(R.color.surfaceBrown))
                    ivNotifIcon.setImageResource(R.drawable.ic_announcement)
                }
                NotificationType.SUCCESS -> {
                    iconContainer.setCardBackgroundColor(context.getColor(R.color.badgeGreen))
                    ivNotifIcon.setImageResource(R.drawable.ic_check_circle)
                }
            }
        }
    }

    override fun getItemCount() = notifications.size
}