package com.pab.scoutify.model

import com.google.gson.annotations.SerializedName

data class DashboardSummary(
    @SerializedName("totalMembers") val totalMembers: Int,
    @SerializedName("totalActivities") val totalActivities: Int,
    @SerializedName("todayAttendance") val todayAttendance: Int,
    @SerializedName("totalTrainers") val totalTrainers: Int
)

data class UpcomingActivity(
    @SerializedName("id") val id: Long,
    @SerializedName("title") val title: String,
    @SerializedName("date") val date: String,
    @SerializedName("location") val location: String,
    @SerializedName("status") val status: String,
    @SerializedName("imageUrl") val imageUrl: String
)

data class NotificationItem(
    @SerializedName("id") val id: Long,
    @SerializedName("title") val title: String,
    @SerializedName("content") val content: String,
    @SerializedName("time") val time: String,
    @SerializedName("category") val category: String
)

data class DashboardUiState(
    val userProfile: ProfileData? = null,
    val summary: DashboardSummary? = null,
    val upcomingActivities: List<UpcomingActivity> = emptyList(),
    val latestNotifications: List<NotificationItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
