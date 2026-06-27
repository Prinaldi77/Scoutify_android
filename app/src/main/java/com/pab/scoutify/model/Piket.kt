package com.pab.scoutify.model

import com.google.gson.annotations.SerializedName

data class Piket(
    val id: Long,
    val hari: String,
    @SerializedName("regu_putra") val reguPutra: String,
    @SerializedName("regu_putri") val reguPutri: String
)
