package com.pab.scoutify.data.repository

import com.pab.scoutify.api.ApiService
import com.pab.scoutify.model.ActivityParticipation
import com.pab.scoutify.model.AttendanceReportSummary
import com.pab.scoutify.model.ScoutParticipation
import com.pab.scoutify.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportRepository @Inject constructor(
    private val apiService: ApiService
) {
    fun getAttendanceSummary(dateRange: String, troop: String): Flow<Resource<AttendanceReportSummary>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.getAttendanceSummary(dateRange, troop)
            if (response.isSuccessful) {
                val data = response.body()?.data
                if (data != null) {
                    emit(Resource.Success(data))
                } else {
                    emit(Resource.Error("Data ringkasan tidak ditemukan"))
                }
            } else {
                emit(Resource.Error("Gagal memuat ringkasan kehadiran"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Terjadi kesalahan"))
        }
    }

    fun getReportActivities(dateRange: String, troop: String): Flow<Resource<List<ActivityParticipation>>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.getReportActivities(dateRange, troop)
            if (response.isSuccessful) {
                emit(Resource.Success(response.body()?.data ?: emptyList()))
            } else {
                emit(Resource.Error("Gagal memuat riwayat aktivitas"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Terjadi kesalahan"))
        }
    }

    fun getTopScouts(): Flow<Resource<List<ScoutParticipation>>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.getTopScouts()
            if (response.isSuccessful) {
                emit(Resource.Success(response.body()?.data ?: emptyList()))
            } else {
                emit(Resource.Error("Gagal memuat leaderboard"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Terjadi kesalahan"))
        }
    }
}
