package com.pab.scoutify.model

import com.google.gson.annotations.SerializedName

data class SelfieVerificationResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("verified") val verified: Boolean,
    @SerializedName("message") val message: String
)

data class SelfieUiState(
    val isFaceDetected: Boolean = false,
    val faceDetectionMessage: String = "Posisikan wajah Anda di dalam area lingkaran",
    val isReadyForVerification: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val verificationSuccess: Boolean = false,
    val capturedImageUri: String? = null
)
