package com.pab.scoutify.model

import com.google.gson.annotations.SerializedName

data class ProfileData(
    val id: Int,
    val name: String,
    val email: String,
    val role: String?,
    val phone: String?,
    val rank: String?,
    val regu: String?,
    val avatar: String?,
    @SerializedName("gugusDepan")
    val gugusDepan: String?,
    @SerializedName("nomorInduk")
    val nomorInduk: String?,
    @SerializedName("jabatan")
    val jabatan: String?,
    @SerializedName("isActive")
    val isActive: Boolean?
)
