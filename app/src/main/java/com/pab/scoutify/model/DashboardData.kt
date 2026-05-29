package com.pab.scoutify.model

import com.google.gson.annotations.SerializedName

data class DashboardData(
    @SerializedName("ringkasan")
    val ringkasan: RingkasanData?,
    
    @SerializedName("statistik_absensi")
    val statistikAbsensi: StatistikAbsensiData?
)

data class RingkasanData(
    @SerializedName("total_anggota_aktif")
    val totalMembers: Int?,
    
    @SerializedName("total_kegiatan")
    val totalActivities: Int?
)

data class StatistikAbsensiData(
    @SerializedName("persentase_kehadiran")
    val attendancePercentage: Double?
)
