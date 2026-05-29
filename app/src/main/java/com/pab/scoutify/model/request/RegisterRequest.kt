package com.pab.scoutify.model.request

import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    @SerializedName("name")
    val name: String,
    
    @SerializedName("email")
    val email: String,
    
    @SerializedName("no_gudep")
    val noGudep: String,
    
    @SerializedName("password")
    val pass: String
)
