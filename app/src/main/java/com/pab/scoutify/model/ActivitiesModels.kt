package com.pab.scoutify.model

import com.google.gson.annotations.SerializedName

data class ActivityItem(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("date") val date: String,
    @SerializedName("location") val location: String,
    @SerializedName("imageUrl") val imageUrl: String,
    @SerializedName("status") val status: String, // e.g., "Berlangsung", "Daftar"
    @SerializedName("type") val type: String, // e.g., "A", "B", "C"
    @SerializedName("category") val category: String, // e.g., "Aktif", "Selesai", "Mendatang"
    @SerializedName("time") val time: String? = null,
    @SerializedName("venue") val venue: String? = null,
    @SerializedName("isInternal") val isInternal: Boolean = false,
    @SerializedName("subStatus") val subStatus: String? = null,
    @SerializedName("participants") val participants: List<String> = emptyList()
)

data class ActivitiesUiState(
    val activities: List<ActivityItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedCategory: String = "Aktif"
)
