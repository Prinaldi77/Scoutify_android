package com.pab.scoutify.data.repository

import com.pab.scoutify.api.SelfieApiService
import com.pab.scoutify.model.SelfieVerificationResponse
import com.pab.scoutify.utils.Resource
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SelfieVerificationRepository @Inject constructor(
    private val apiService: SelfieApiService
) {
    suspend fun uploadSelfie(
        activityId: Long,
        attendanceId: Long,
        latitude: Double,
        longitude: Double,
        imageFile: File
    ): Resource<SelfieVerificationResponse> {
        return try {
            val activityIdBody = activityId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val attendanceIdBody = attendanceId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val latBody = latitude.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val lngBody = longitude.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            
            val requestFile = imageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val selfiePart = MultipartBody.Part.createFormData("selfieImage", imageFile.name, requestFile)

            val response = apiService.uploadSelfie(
                activityIdBody,
                attendanceIdBody,
                latBody,
                lngBody,
                selfiePart
            )

            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Gagal mengunggah foto selfie")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Terjadi kesalahan koneksi")
        }
    }
}
