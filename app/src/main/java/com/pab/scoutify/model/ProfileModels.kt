package com.pab.scoutify.model

import com.google.gson.annotations.SerializedName

data class UpdateProfileRequest(
    val fullName: String,
    val phoneNumber: String,
    val email: String,
    val address: String? = null
)

data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String,
    val confirmPassword: String
)

data class ProfileUiState(
    val profile: ProfileData? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val updateSuccess: Boolean = false,
    val logoutSuccess: Boolean = false
)
