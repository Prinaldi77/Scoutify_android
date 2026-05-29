package com.pab.scoutify.model

import com.google.gson.annotations.SerializedName

data class BaseResponse<T>(
    @SerializedName("status")
    val success: Boolean,
    val message: String,
    val data: T
)
