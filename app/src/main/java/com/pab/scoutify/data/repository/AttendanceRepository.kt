package com.pab.scoutify.data.repository

import com.pab.scoutify.api.ApiService
import com.pab.scoutify.api.AttendanceApiService
import com.pab.scoutify.model.ActiveActivity
import com.pab.scoutify.model.AttendanceStatus
import com.pab.scoutify.model.BaseResponse
import com.pab.scoutify.model.request.CheckOutRequest
import com.pab.scoutify.utils.Resource
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AttendanceRepository @Inject constructor(
    private val apiService: AttendanceApiService,
    private val mainApiService: ApiService
) {
    suspend fun getCurrentActivity(): Resource<ActiveActivity> {
        return try {
            val response = apiService.getCurrentActivity()
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Gagal mendapatkan kegiatan aktif")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Terjadi kesalahan")
        }
    }

    suspend fun checkIn(
        latitude: RequestBody,
        longitude: RequestBody,
        accuracy: RequestBody,
        kegiatanId: RequestBody,
        selfie: MultipartBody.Part?
    ): Resource<BaseResponse<Any>> {
        return try {
            val response = mainApiService.checkIn(latitude, longitude, accuracy, kegiatanId, selfie)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Error: ${response.code()} ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Terjadi kesalahan koneksi")
        }
    }

    suspend fun checkOut(request: CheckOutRequest): Resource<BaseResponse<Any>> {
        return try {
            val response = mainApiService.checkOut(request)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Error: ${response.code()} ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Terjadi kesalahan koneksi")
        }
    }

    suspend fun submitPermit(
        kegiatanId: RequestBody,
        reason: RequestBody,
        type: RequestBody,
        document: MultipartBody.Part?
    ): Resource<BaseResponse<Any>> {
        return try {
            val response = mainApiService.submitPermit(kegiatanId, reason, type, document)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Error: ${response.code()} ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Terjadi kesalahan koneksi")
        }
    }

    suspend fun getTodayAttendance(): Resource<BaseResponse<Any>> {
        return try {
            val response = mainApiService.getTodayAttendance()
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Error: ${response.code()} ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Terjadi kesalahan koneksi")
        }
    }

    suspend fun getAttendanceStatus(): Resource<AttendanceStatus> {
        return try {
            val response = apiService.getAttendanceStatus()
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Gagal mendapatkan status absensi")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Terjadi kesalahan")
        }
    }
}
