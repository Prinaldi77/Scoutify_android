package com.pab.scoutify.api

import com.pab.scoutify.model.*
import com.pab.scoutify.model.request.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ─── AUTH ───────────────────────────────────────────────
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<Any>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<BaseResponse<Any>>

    @POST("auth/refresh-token")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<BaseResponse<LoginResponse>>

    @POST("auth/logout")
    suspend fun logout(): Response<BaseResponse<Any>>

    // ─── DASHBOARD ──────────────────────────────────────────
    @GET("dashboard")
    suspend fun getDashboard(): Response<BaseResponse<DashboardData>>

    // ─── ATTENDANCE ─────────────────────────────────────────
    @GET("attendance/current-activity")
    suspend fun getCurrentActivity(): Response<BaseResponse<ActiveActivity>>

    @GET("attendance/status")
    suspend fun getAttendanceStatus(): Response<BaseResponse<AttendanceStatus>>

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

    @POST("kegiatan")
    suspend fun createKegiatan(@Body kegiatan: Kegiatan): Response<BaseResponse<Kegiatan>>

    @GET("kegiatan/{id}")
    suspend fun getKegiatanDetail(@Path("id") id: Long): Response<BaseResponse<Kegiatan>>

    @PUT("kegiatan/{id}")
    suspend fun updateKegiatan(@Path("id") id: Long, @Body kegiatan: Kegiatan): Response<BaseResponse<Kegiatan>>

    @DELETE("kegiatan/{id}")
    suspend fun deleteKegiatan(@Path("id") id: Long): Response<BaseResponse<Any>>

    // ─── MEMBERS ────────────────────────────────────────────
    @GET("anggota")
    suspend fun getAnggotas(): Response<BaseResponse<List<Anggota>>>

    @GET("anggota/{id}")
    suspend fun getMemberDetail(@Path("id") id: Int): Response<BaseResponse<MemberDetail>>

    @POST("anggota")
    suspend fun createAnggota(@Body anggota: Anggota): Response<BaseResponse<Anggota>>

    @PUT("anggota/{id}")
    suspend fun updateAnggota(@Path("id") id: Int, @Body anggota: Anggota): Response<BaseResponse<Anggota>>

    @PUT("anggota/{id}/reset-password")
    suspend fun resetMemberPassword(@Path("id") id: Int): Response<BaseResponse<Any>>

    // ─── PROFILE ────────────────────────────────────────────
    @GET("profile")
    suspend fun getProfile(): Response<BaseResponse<ProfileData>>

    @PUT("profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<BaseResponse<ProfileData>>

    @Multipart
    @POST("profile/avatar")
    suspend fun uploadAvatar(@Part avatar: MultipartBody.Part): Response<BaseResponse<ProfileData>>

    @PUT("profile/change-password")
    suspend fun changePassword(@Body request: com.pab.scoutify.model.request.ChangePasswordRequest): Response<BaseResponse<Any>>

    // ─── REPORTS ────────────────────────────────────────────
    @GET("reports/attendance/summary")
    suspend fun getAttendanceSummary(
        @Query("dateRange") dateRange: String?,
        @Query("troop") troop: String?
    ): Response<BaseResponse<AttendanceReportSummary>>

    @GET("reports/attendance/activities")
    suspend fun getReportActivities(
        @Query("dateRange") dateRange: String?,
        @Query("troop") troop: String?
    ): Response<BaseResponse<List<ActivityParticipation>>>

    @GET("reports/attendance/top-scouts")
    suspend fun getTopScouts(): Response<BaseResponse<List<ScoutParticipation>>>

    // ─── NOTIFICATIONS ──────────────────────────────────────
    @GET("notifications")
    suspend fun getNotifications(): Response<BaseResponse<List<Notification>>>

    @PUT("notifications/{id}/read")
    suspend fun markNotificationRead(@Path("id") id: Long): Response<BaseResponse<Any>>

    @PUT("notifications/read-all")
    suspend fun markAllNotificationsRead(): Response<BaseResponse<Any>>
}
