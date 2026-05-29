package com.pab.scoutify.model.request

data class UpdateProfileRequest(
    val name: String,
    val password: String? = null
)
