package com.pab.scoutify.data.repository

import com.pab.scoutify.api.RetrofitClient
import com.pab.scoutify.model.BaseResponse
import com.pab.scoutify.model.DashboardData
import com.pab.scoutify.model.Kegiatan
import com.pab.scoutify.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

class DashboardRepository {

    private val apiService = RetrofitClient.instance

    suspend fun getDashboard(): Resource<BaseResponse<DashboardData>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getDashboard()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Resource.Success(body)
                } else {
                    Resource.Error("Data dashboard kosong")
                }
            } else {
                Resource.Error("Gagal mengambil data dashboard: ${response.code()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Terjadi kesalahan koneksi")
        }
    }

    suspend fun getKegiatan(): Resource<BaseResponse<List<Kegiatan>>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getKegiatan()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Resource.Success(body)
                } else {
                    Resource.Error("Data kegiatan kosong")
                }
            } else {
                Resource.Error("Gagal mengambil data kegiatan: ${response.code()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Terjadi kesalahan koneksi")
        }
    }

    suspend fun createKegiatan(kegiatan: Kegiatan): Resource<BaseResponse<Kegiatan>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.createKegiatan(kegiatan)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Resource.Success(body)
                } else {
                    Resource.Error("Respon kosong dari server")
                }
            } else {
                Resource.Error("Gagal menambahkan kegiatan: ${response.code()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Terjadi kesalahan koneksi")
        }
    }

    suspend fun checkIn(request: com.pab.scoutify.model.request.AbsensiRequest): Resource<BaseResponse<Any>> = withContext(Dispatchers.IO) {
        try {
            val lat = request.latitude.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val lng = request.longitude.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val acc = "10.0".toRequestBody("text/plain".toMediaTypeOrNull())
            val kid = (request.kegiatanId ?: 0).toString().toRequestBody("text/plain".toMediaTypeOrNull())
            
            val response = apiService.checkIn(lat, lng, acc, kid, null)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Resource.Success(body)
                } else {
                    Resource.Error("Respon kosong dari server")
                }
            } else {
                Resource.Error("Gagal melakukan absensi: ${response.code()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Terjadi kesalahan koneksi")
        }
    }

    suspend fun getAnggotas(): Resource<BaseResponse<List<com.pab.scoutify.model.Anggota>>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getAnggotas()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Resource.Success(body)
                } else {
                    Resource.Error("Data anggota kosong")
                }
            } else {
                Resource.Error("Gagal mengambil data anggota: ${response.code()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Terjadi kesalahan koneksi")
        }
    }
}
