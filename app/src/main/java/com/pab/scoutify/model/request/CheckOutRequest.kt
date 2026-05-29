package com.pab.scoutify.model.request

import com.google.gson.annotations.SerializedName

data class CheckOutRequest(
    @SerializedName("kegiatanId")
    val kegiatanId: Long,
    @SerializedName("latitude")
    val latitude: Double,
    @SerializedName("longitude")
    val longitude: Double,
    @SerializedName("accuracy")
    val accuracy: Double,
    @SerializedName("isMockLocation")
    val isMockLocation: Boolean,
    @SerializedName("deviceId")
    val deviceId: String
)
