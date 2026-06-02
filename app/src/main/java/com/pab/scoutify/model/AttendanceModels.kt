package com.pab.scoutify.model

import com.google.gson.annotations.SerializedName

data class ActiveActivity(
    val id: Long,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val radius: Int,
    val locationName: String? = null,
    val timeRange: String? = null
)

data class CheckInRequest(
    @SerializedName("activityId") val activityId: Long,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("accuracy") val accuracy: Float,
    @SerializedName("timestamp") val timestamp: Long
)

data class AttendanceStatus(
    val status: String, // e.g., "Sudah Check In", "Belum Check In", "Terlambat", "Izin", "Sakit"
    val checkInTime: String? = null
)

data class AttendanceUiState(
    val activeActivity: ActiveActivity? = null,
    val userLocation: UserLocation? = null,
    val isWithinRadius: Boolean = false,
    val distance: Double = 0.0,
    val attendanceStatus: String = "Belum Check In",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val checkInSuccess: Boolean = false
)

data class UserLocation(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float
)
