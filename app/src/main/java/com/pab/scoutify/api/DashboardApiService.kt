package com.pab.scoutify.api

import com.pab.scoutify.model.*
import retrofit2.Response
import retrofit2.http.GET

interface DashboardApiService {
    @GET("profile/me")
    suspend fun getProfile(): Response<BaseResponse<ProfileData>>

    @GET("dashboard/summary")
    suspend fun getDashboardSummary(): Response<BaseResponse<DashboardSummary>>

    @GET("activities/upcoming")
    suspend fun getUpcomingActivities(): Response<BaseResponse<List<UpcomingActivity>>>

    @GET("notifications/latest")
    suspend fun getLatestNotifications(): Response<BaseResponse<List<NotificationItem>>>

    @GET("dashboard")
    suspend fun getDashboard(): Response<BaseResponse<DashboardData>>
}
