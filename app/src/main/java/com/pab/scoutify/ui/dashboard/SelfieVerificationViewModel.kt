package com.pab.scoutify.ui.dashboard

import android.content.Context
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pab.scoutify.data.repository.SelfieVerificationRepository
import com.pab.scoutify.model.SelfieUiState
import com.pab.scoutify.utils.FaceDetectionHelper
import com.pab.scoutify.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class SelfieVerificationViewModel @Inject constructor(
    private val repository: SelfieVerificationRepository,
    private val faceDetectionHelper: FaceDetectionHelper,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SelfieUiState())
    val uiState = _uiState.asStateFlow()

    fun onImageAnalyzed(imageProxy: ImageProxy, width: Int, height: Int) {
        viewModelScope.launch {
            val faces = faceDetectionHelper.detectFaces(imageProxy)
            if (faces.isEmpty()) {
                _uiState.update { it.copy(
                    isFaceDetected = false,
                    isReadyForVerification = false,
                    faceDetectionMessage = "Wajah tidak terdeteksi"
                ) }
            } else if (faces.size > 1) {
                _uiState.update { it.copy(
                    isFaceDetected = false,
                    isReadyForVerification = false,
                    faceDetectionMessage = "Hanya boleh satu wajah"
                ) }
            } else {
                val face = faces[0]
                val isInOval = faceDetectionHelper.isFaceInOval(face, width, height)
                
                if (isInOval) {
                    _uiState.update { it.copy(
                        isFaceDetected = true,
                        isReadyForVerification = true,
                        faceDetectionMessage = "Siap untuk verifikasi"
                    ) }
                } else {
                    _uiState.update { it.copy(
                        isFaceDetected = false,
                        isReadyForVerification = false,
                        faceDetectionMessage = "Posisikan wajah di dalam oval"
                    ) }
                }
            }
        }
    }

    fun captureAndUpload(
        imageCapture: ImageCapture,
        activityId: Long,
        attendanceId: Long,
        latitude: Double,
        longitude: Double
    ) {
        val cacheDir = context.externalCacheDir ?: context.cacheDir
        val photoFile = File(
            cacheDir,
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis()) + ".jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        _uiState.update { it.copy(isLoading = true) }

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    uploadSelfie(activityId, attendanceId, latitude, longitude, photoFile)
                }

                override fun onError(exception: ImageCaptureException) {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Gagal mengambil foto: ${exception.message}") }
                }
            }
        )
    }

    private fun uploadSelfie(
        activityId: Long,
        attendanceId: Long,
        latitude: Double,
        longitude: Double,
        file: File
    ) {
        viewModelScope.launch {
            val result = repository.uploadSelfie(activityId, attendanceId, latitude, longitude, file)
            when (result) {
                is Resource.Success -> {
                    if (result.data.success && result.data.verified) {
                        _uiState.update { it.copy(verificationSuccess = true) }
                    } else {
                        _uiState.update { it.copy(errorMessage = result.data.message) }
                    }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(errorMessage = result.message) }
                }
                else -> {}
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun resetError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
