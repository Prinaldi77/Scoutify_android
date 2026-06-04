package com.pab.scoutify.model

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("success") val success: Boolean?,
    @SerializedName("message") val message: String?,
    @SerializedName("user") val user: User?,
    @SerializedName("tokens") val tokens: Tokens?,
    
    // Support untuk format "Flat JSON" (token langsung di level atas)
    @SerializedName("accessToken") val accessTokenDirect: String?,
    @SerializedName("access_token") val accessTokenSnake: String?,
    @SerializedName("token") val tokenSimple: String?,

    // Support untuk format nested "data"
    @SerializedName("data") val data: NestedLoginData?
) {
    // Helper untuk mengambil token dari field mana pun yang tersedia
    val anyToken: String? get() = tokens?.accessToken ?: accessTokenDirect ?: accessTokenSnake ?: tokenSimple ?: data?.anyToken
    val anyUser: User? get() = user ?: data?.user
}

data class NestedLoginData(
    @SerializedName("accessToken") val accessTokenDirect: String?,
    @SerializedName("access_token") val accessTokenSnake: String?,
    @SerializedName("token") val tokenSimple: String?,
    @SerializedName("tokens") val tokens: Tokens?,
    @SerializedName("user") val user: User?
) {
    val anyToken: String? get() = tokens?.accessToken ?: accessTokenDirect ?: accessTokenSnake ?: tokenSimple
}

data class Tokens(
    @SerializedName("accessToken") val accessToken: String?,
    @SerializedName("refreshToken") val refreshToken: String?
)

data class User(
    @SerializedName("id") val id: Any?,
    @SerializedName("name") val name: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("role") val role: String?,
    @SerializedName("phone") val phone: String?,
    @SerializedName("rank") val rank: String?,
    @SerializedName("regu") val regu: String?,
    @SerializedName("gugus_depan") val gugusDepan: String?,
    @SerializedName("nomor_induk") val nomorInduk: String?,
    @SerializedName("jabatan") val jabatan: String?
)

