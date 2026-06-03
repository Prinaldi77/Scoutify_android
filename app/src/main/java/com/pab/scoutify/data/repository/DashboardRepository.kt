package com.pab.scoutify.data.repository

import com.pab.scoutify.api.DashboardApiService
import com.pab.scoutify.model.*
import com.pab.scoutify.utils.Resource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DashboardRepository @Inject constructor(
    private val apiService: DashboardApiService
) {
    suspend fun getProfile(): ProfileData? {
        val response = apiService.getProfile()
        return if (response.isSuccessful) response.body()?.data else null
    }

    suspend fun getDashboardSummary(): DashboardSummary? {
        val response = apiService.getDashboardSummary()
        return if (response.isSuccessful) response.body()?.data else null
    }

    suspend fun getUpcomingActivities(): List<UpcomingActivity> {
        val response = apiService.getUpcomingActivities()
        return if (response.isSuccessful) response.body()?.data ?: emptyList() else emptyList()
    }

    suspend fun getLatestNotifications(): List<NotificationItem> {
        val response = apiService.getLatestNotifications()
        return if (response.isSuccessful) response.body()?.data ?: emptyList() else emptyList()
    }

    suspend fun getDashboard(): Resource<BaseResponse<DashboardData>> {
        return try {
            val response = apiService.getDashboard()
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Gagal memuat dashboard")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Terjadi kesalahan")
        }
    }
}
