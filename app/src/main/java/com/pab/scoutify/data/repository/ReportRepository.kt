package com.pab.scoutify.data.repository

import com.pab.scoutify.api.ApiService
import com.pab.scoutify.model.*
import com.pab.scoutify.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportRepository @Inject constructor(
    private val apiService: ApiService
) {
    fun getAttendanceSummary(dateRange: String?, troop: String?): Flow<Resource<AttendanceReportSummary>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.getAttendanceSummary(dateRange, troop)
            val body = response.body()
            if (response.isSuccessful && body != null && body.success) {
                emit(Resource.Success(body.data))
            } else {
                emit(Resource.Error(body?.message ?: "Gagal memuat ringkasan"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Terjadi kesalahan"))
        }
    }

    fun getReportActivities(dateRange: String?, troop: String?): Flow<Resource<List<ActivityParticipation>>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.getReportActivities(dateRange, troop)
            val body = response.body()
            if (response.isSuccessful && body != null && body.success) {
                emit(Resource.Success(body.data))
            } else {
                emit(Resource.Error(body?.message ?: "Gagal memuat aktivitas"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Terjadi kesalahan"))
        }
    }

    fun getTopScouts(): Flow<Resource<List<ScoutParticipation>>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.getTopScouts()
            val body = response.body()
            if (response.isSuccessful && body != null && body.success) {
                emit(Resource.Success(body.data))
            } else {
                emit(Resource.Error(body?.message ?: "Gagal memuat leaderboard"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Terjadi kesalahan"))
        }
    }
}
