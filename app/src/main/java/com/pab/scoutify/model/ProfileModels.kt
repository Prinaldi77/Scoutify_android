package com.pab.scoutify.model

import com.google.gson.annotations.SerializedName

// UpdateProfileRequest has been moved to com.pab.scoutify.model.request.UpdateProfileRequest

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
