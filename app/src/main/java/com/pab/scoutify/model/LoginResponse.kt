package com.pab.scoutify.model

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("success")
    val success: Boolean,
    val message: String?,
    val data: LoginData?
)

data class LoginData(
    val user: User?,
    val tokens: Tokens?
)

data class Tokens(
    val accessToken: String,
    val refreshToken: String
)

data class User(
    val id: Int,
    val name: String,
    val email: String,
    val role: String?,
    val phone: String?,
    val avatar: String?,
    @SerializedName("gugusDepan")
    val gugusDepan: String?,
    @SerializedName("nomorInduk")
    val nomorInduk: String?,
    @SerializedName("jabatan")
    val jabatan: String?
)
