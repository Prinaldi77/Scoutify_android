package com.pab.scoutify.data.repository

import com.pab.scoutify.api.ApiService
import com.pab.scoutify.model.*
import com.pab.scoutify.model.request.CheckOutRequest
import com.pab.scoutify.utils.Resource
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AttendanceRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getCurrentActivity(): Resource<ActiveActivity> {
        return try {
            val response = apiService.getCurrentActivity()
            val body = response.body()
            if (response.isSuccessful && body != null && body.success) {
                Resource.Success(body.data)
            } else {
                Resource.Error(body?.message ?: "Tidak ada kegiatan aktif")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Terjadi kesalahan koneksi")
        }
    }

    suspend fun getAttendanceStatus(): Resource<AttendanceStatus> {
        return try {
            val response = apiService.getAttendanceStatus()
            val body = response.body()
            if (response.isSuccessful && body != null && body.success) {
                Resource.Success(body.data)
            } else {
                Resource.Error(body?.message ?: "Gagal mendapatkan status")
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
            val response = apiService.checkIn(latitude, longitude, accuracy, kegiatanId, selfie)
            val body = response.body()
            if (response.isSuccessful && body != null) {
                Resource.Success(body)
            } else {
                Resource.Error("Check-in gagal: ${response.code()}")
            }
        } catch (e: Exception) {
            Resource.Error("Kesalahan jaringan: ${e.localizedMessage}")
        }
    }

    suspend fun getTodayAttendance(): Resource<BaseResponse<Any>> {
        return try {
            val response = apiService.getTodayAttendance()
            val body = response.body()
            if (response.isSuccessful && body != null) {
                Resource.Success(body)
            } else {
                Resource.Error("Gagal mengambil data")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Error")
        }
    }

    suspend fun submitPermit(
        kegiatanId: RequestBody,
        reason: RequestBody,
        type: RequestBody,
        document: MultipartBody.Part?
    ): Resource<BaseResponse<Any>> {
        return try {
            val response = apiService.submitPermit(kegiatanId, reason, type, document)
            val body = response.body()
            if (response.isSuccessful && body != null) {
                Resource.Success(body)
            } else {
                Resource.Error(body?.message ?: "Gagal mengirim surat izin: ${response.code()}")
            }
        } catch (e: Exception) {
            Resource.Error("Kesalahan jaringan: ${e.localizedMessage}")
        }
    }
}
