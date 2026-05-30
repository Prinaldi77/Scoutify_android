package com.pab.scoutify.model.request

import com.google.gson.annotations.SerializedName

class LoginRequest(
    @field:SerializedName("email") val email: String?,
    @field:SerializedName("password") val password: String?,
    @field:SerializedName("deviceId") val deviceId: String? = null,
    @field:SerializedName("deviceBrand") val deviceBrand: String? = null,
    @field:SerializedName("deviceModel") val deviceModel: String? = null,
    @field:SerializedName("androidVersion") val androidVersion: String? = null
)
