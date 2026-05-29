package com.pab.scoutify.model

data class AttendanceSession(
    val id: Long,
    val title: String,
    val date: String,
    val presentCount: Int,
    val totalCount: Int,
    val latitude: Double,
    val longitude: Double,
    val radiusInMeters: Float,
    val isPresent: Boolean = false
) {
    val progress: Int
        get() = if (totalCount > 0) (presentCount * 100) / totalCount else 0
}
