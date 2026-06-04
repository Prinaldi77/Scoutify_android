package com.pab.scoutify.model.request

import com.google.gson.annotations.SerializedName

data class UpdateProfileRequest(
    @SerializedName("name") val name: String? = null,
    @SerializedName("fullName") val fullName: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("phoneNumber") val phoneNumber: String? = null,
    @SerializedName("address") val address: String? = null,
    @SerializedName("password") val password: String? = null,
    @SerializedName("gugusDepan") val gugusDepan: String? = null,
    @SerializedName("nomorInduk") val nomorInduk: String? = null,
    @SerializedName("jabatan") val jabatan: String? = null
)
