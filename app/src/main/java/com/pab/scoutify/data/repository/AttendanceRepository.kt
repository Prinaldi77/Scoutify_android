package com.pab.scoutify.data.repository

import com.pab.scoutify.api.RetrofitClient
import com.pab.scoutify.model.BaseResponse
import com.pab.scoutify.model.request.CheckOutRequest
import com.pab.scoutify.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody

class AttendanceRepository {

    private val apiService = RetrofitClient.instance

    suspend fun checkIn(
        latitude: RequestBody,
        longitude: RequestBody,
        accuracy: RequestBody,
        kegiatanId: RequestBody,
        selfie: MultipartBody.Part?
    ): Resource<BaseResponse<Any>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.checkIn(latitude, longitude, accuracy, kegiatanId, selfie)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Resource.Success(body)
                } else {
                    Resource.Error("Respon kosong dari server")
                }
            } else {
                val errorMsg = parseErrorBody(response.errorBody()?.string(), response.code())
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Terjadi kesalahan koneksi")
        }
    }

    suspend fun checkOut(request: CheckOutRequest): Resource<BaseResponse<Any>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.checkOut(request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Resource.Success(body)
                } else {
                    Resource.Error("Respon kosong dari server")
                }
            } else {
                val errorMsg = parseErrorBody(response.errorBody()?.string(), response.code())
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Terjadi kesalahan koneksi")
        }
    }

    suspend fun getTodayAttendance(): Resource<BaseResponse<Any>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getTodayAttendance()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Resource.Success(body)
                } else {
                    Resource.Error("Respon kosong dari server")
                }
            } else {
                Resource.Error("Gagal mengambil status hari ini: ${response.code()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Terjadi kesalahan koneksi")
        }
    }

    suspend fun getAttendanceHistory(): Resource<BaseResponse<List<Any>>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getAttendanceHistory()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Resource.Success(body)
                } else {
                    Resource.Error("Respon kosong dari server")
                }
            } else {
                Resource.Error("Gagal mengambil riwayat kehadiran: ${response.code()}")
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
    ): Resource<BaseResponse<Any>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.submitPermit(kegiatanId, reason, type, document)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Resource.Success(body)
                } else {
                    Resource.Error("Respon kosong dari server")
                }
            } else {
                val errorMsg = parseErrorBody(response.errorBody()?.string(), response.code())
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Terjadi kesalahan koneksi")
        }
    }

    private fun parseErrorBody(errorJson: String?, code: Int): String {
        return try {
            if (errorJson != null) {
                val jsonObject = org.json.JSONObject(errorJson)
                jsonObject.getString("message")
            } else {
                "Gagal dengan kode: $code"
            }
        } catch (e: Exception) {
            "Gagal dengan kode: $code"
        }
    }
}
