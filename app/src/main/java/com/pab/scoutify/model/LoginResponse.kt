package com.pab.scoutify.model

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("success") val success: Boolean?,
    @SerializedName("message") val message: String?,
    // Jika backend mengirim user & tokens langsung di luar, pakai ini:
    @SerializedName("user") val user: User?,
    @SerializedName("tokens") val tokens: Tokens?
)

data class Tokens(
    @SerializedName("accessToken") val accessToken: String?,
    @SerializedName("refreshToken") val refreshToken: String?
)

data class User(
    @SerializedName("id") val id: Any?, // Gunakan Any? karena tadi ada urusan BigInt
    @SerializedName("name") val name: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("role") val role: String?,
    @SerializedName("phone") val phone: String?,
    @SerializedName("rank") val rank: String?,
    @SerializedName("regu") val regu: String?
)