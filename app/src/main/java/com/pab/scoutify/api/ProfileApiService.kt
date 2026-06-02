package com.pab.scoutify.api

import com.pab.scoutify.model.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ProfileApiService {
    @GET("profile/me")
    suspend fun getProfile(): Response<ProfileData>

    @PUT("profile/update")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<BaseResponse<ProfileData>>

    @Multipart
    @PUT("profile/photo")
    suspend fun uploadPhoto(@Part photo: MultipartBody.Part): Response<BaseResponse<ProfileData>>

    @PUT("profile/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<BaseResponse<Any>>

    @POST("auth/logout")
    suspend fun logout(): Response<BaseResponse<Any>>
}
