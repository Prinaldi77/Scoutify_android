package com.pab.scoutify.model

import com.google.gson.annotations.SerializedName

data class MemberDetail(
    val id: Int,
    val name: String,
    val rank: String,
    val nisn: String,
    @SerializedName("foto_url")
    val fotoUrl: String?,
    val status: String, // Online/Verified
    val birthInfo: String?, // Place / Date of Birth
    val phone: String?,
    val address: String?,
    val bloodType: String?,
    val regu: String?,
    val achievements: List<Achievement> = emptyList(),
    val activityHistory: List<ActivityHistoryItem> = emptyList(),
    val attendanceRate: Int = 0,
    val email: String? = null,
    val role: String? = null,
    val jabatan: String? = null
)

data class Achievement(
    val id: Int,
    val title: String,
    val date: String,
    val icon: String? = null
)

data class ActivityHistoryItem(
    val id: Int,
    val title: String,
    val date: String,
    val status: String, // Present, Late, Absent
    val type: String // Icon category
)

data class MemberDetailUiState(
    val isLoading: Boolean = false,
    val member: MemberDetail? = null,
    val errorMessage: String? = null,
    val resetPasswordSuccess: Boolean = false
)
