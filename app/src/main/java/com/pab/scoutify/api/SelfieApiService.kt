package com.pab.scoutify.api

import com.pab.scoutify.model.SelfieVerificationResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface SelfieApiService {
    @Multipart
    @POST("attendance/selfie-verification")
    suspend fun uploadSelfie(
        @Part("activityId") activityId: RequestBody,
        @Part("attendanceId") attendanceId: RequestBody,
        @Part("latitude") latitude: RequestBody,
        @Part("longitude") longitude: RequestBody,
        @Part selfieImage: MultipartBody.Part
    ): Response<SelfieVerificationResponse>
}
