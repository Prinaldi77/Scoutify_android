package com.pab.scoutify.model

data class Notification(
    val id: Int,
    val title: String,
    val description: String,
    val time: String,
    val type: NotificationType
)

enum class NotificationType {
    WARNING, INFO, UPDATE, SUCCESS
}