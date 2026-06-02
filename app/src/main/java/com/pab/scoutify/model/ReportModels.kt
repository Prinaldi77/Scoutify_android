package com.pab.scoutify.model

import com.google.gson.annotations.SerializedName

data class AttendanceReportSummary(
    val overallRate: Int,
    val distribution: AttendanceDistribution,
    val weeklyTrend: List<WeeklyParticipation>
)

data class AttendanceDistribution(
    val hadir: Int,
    val izin: Int,
    val sakit: Int,
    val alpa: Int
)

data class WeeklyParticipation(
    val week: String,
    val attendance: Int,
    val total: Int
)

data class ActivityParticipation(
    val id: String,
    val title: String,
    val location: String,
    val date: String,
    val rate: Int,
    val iconUrl: String? = null
)

data class ScoutParticipation(
    val name: String,
    val rate: Int,
    val initial: String,
    val color: String // Hex color for the initial background
)

data class ReportUiState(
    val isLoading: Boolean = false,
    val summary: AttendanceReportSummary? = null,
    val recentActivities: List<ActivityParticipation> = emptyList(),
    val topScouts: List<ScoutParticipation> = emptyList(),
    val errorMessage: String? = null,
    val dateRange: String = "Aug 01 - Aug 31",
    val selectedTroop: String = "All Troops"
)
