package com.pab.scoutify.model

import com.google.gson.annotations.SerializedName

data class BaseResponse<T>(
    @SerializedName("success")
    val success: Boolean,
    val message: String,
    val data: T
)
