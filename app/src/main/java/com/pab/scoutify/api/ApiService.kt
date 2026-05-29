package com.pab.scoutify.api

import com.pab.scoutify.model.BaseResponse
import com.pab.scoutify.model.DashboardData
import com.pab.scoutify.model.Kegiatan
import com.pab.scoutify.model.LoginResponse
import com.pab.scoutify.model.Notification
import com.pab.scoutify.model.ProfileData
import com.pab.scoutify.model.request.LoginRequest
import com.pab.scoutify.model.request.RegisterRequest
import com.pab.scoutify.model.request.RefreshTokenRequest
import com.pab.scoutify.model.request.UpdateProfileRequest
import com.pab.scoutify.model.request.CheckOutRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ─── AUTH ───────────────────────────────────────────────
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<BaseResponse<Any>>

    @POST("auth/refresh-token")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<LoginResponse>

    @POST("auth/logout")
    suspend fun logout(): Response<BaseResponse<Any>>

    // ─── DASHBOARD ──────────────────────────────────────────
    @GET("dashboard")
    suspend fun getDashboard(): Response<BaseResponse<DashboardData>>

    // ─── ATTENDANCE ─────────────────────────────────────────
    /**
     * Check-In dengan GPS + Selfie (Multipart)
     * Field: latitude, longitude, accuracy, kegiatanId + selfie file
     */
    @Multipart
    @POST("attendance/checkin")
    suspend fun checkIn(
        @Part("latitude") latitude: RequestBody,
        @Part("longitude") longitude: RequestBody,
        @Part("accuracy") accuracy: RequestBody,
        @Part("kegiatanId") kegiatanId: RequestBody,
        @Part selfie: MultipartBody.Part?
    ): Response<BaseResponse<Any>>

    @POST("attendance/checkout")
    suspend fun checkOut(@Body request: CheckOutRequest): Response<BaseResponse<Any>>

    @GET("attendance/today")
    suspend fun getTodayAttendance(): Response<BaseResponse<Any>>

    @GET("attendance/history")
    suspend fun getAttendanceHistory(): Response<BaseResponse<List<Any>>>

    @GET("attendance/stats/{kegiatanId}")
    suspend fun getAttendanceStats(@Path("kegiatanId") kegiatanId: Long): Response<BaseResponse<Any>>

    @Multipart
    @POST("attendance/permit")
    suspend fun submitPermit(
        @Part("kegiatanId") kegiatanId: RequestBody,
        @Part("reason") reason: RequestBody,
        @Part("type") type: RequestBody,
        @Part document: MultipartBody.Part?
    ): Response<BaseResponse<Any>>

    // ─── KEGIATAN ───────────────────────────────────────────
    @GET("kegiatan")
    suspend fun getKegiatan(): Response<BaseResponse<List<Kegiatan>>>

    @GET("kegiatan/{id}")
    suspend fun getKegiatanDetail(@Path("id") id: Long): Response<BaseResponse<Kegiatan>>

    @POST("kegiatan")
    suspend fun createKegiatan(@Body kegiatan: Kegiatan): Response<BaseResponse<Kegiatan>>

    @PUT("kegiatan/{id}")
    suspend fun updateKegiatan(
        @Path("id") id: Long,
        @Body kegiatan: Kegiatan
    ): Response<BaseResponse<Kegiatan>>

    @DELETE("kegiatan/{id}")
    suspend fun deleteKegiatan(@Path("id") id: Long): Response<BaseResponse<Any>>

    // ─── MEMBERS ────────────────────────────────────────────
    @GET("anggota")
    suspend fun getAnggotas(): Response<BaseResponse<List<com.pab.scoutify.model.Anggota>>>

    // ─── PROFILE ────────────────────────────────────────────
    @GET("profile")
    suspend fun getProfile(): Response<BaseResponse<ProfileData>>

    @PUT("profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<BaseResponse<ProfileData>>

    @Multipart
    @POST("profile/avatar")
    suspend fun uploadAvatar(
        @Part avatar: MultipartBody.Part
    ): Response<BaseResponse<ProfileData>>

    @PUT("profile/change-password")
    suspend fun changePassword(
        @Body request: com.pab.scoutify.model.request.ChangePasswordRequest
    ): Response<BaseResponse<Any>>

    // ─── NOTIFICATIONS ──────────────────────────────────────
    @GET("notifications")
    suspend fun getNotifications(): Response<BaseResponse<List<Notification>>>

    @PUT("notifications/{id}/read")
    suspend fun markNotificationRead(@Path("id") id: Long): Response<BaseResponse<Any>>

    @PUT("notifications/read-all")
    suspend fun markAllNotificationsRead(): Response<BaseResponse<Any>>
}
