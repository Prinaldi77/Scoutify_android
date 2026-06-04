package com.pab.scoutify.data.repository

import com.pab.scoutify.api.ApiService
import com.pab.scoutify.model.BaseResponse
import com.pab.scoutify.model.Kegiatan
import com.pab.scoutify.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActivitiesRepository @Inject constructor(
    private val apiService: ApiService
) {
    fun getActivities(category: String? = null, search: String? = null): Flow<Resource<BaseResponse<List<Kegiatan>>>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.getKegiatan()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    val filteredData = body.data.filter { kegiatan ->
                        val matchCategory = category == null || category == "Semua" || kegiatan.kategori?.contains(category, ignoreCase = true) == true
                        val matchSearch = search == null || kegiatan.nama?.contains(search, ignoreCase = true) == true
                        matchCategory && matchSearch
                    }
                    emit(Resource.Success(body.copy(data = filteredData)))
                } else {
                    emit(Resource.Error("Gagal memuat data: Respons kosong"))
                }
            } else {
                emit(Resource.Error("Error: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Terjadi kesalahan koneksi"))
        }
    }

    fun createActivity(kegiatan: Kegiatan): Flow<Resource<BaseResponse<Kegiatan>>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.createKegiatan(kegiatan)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    emit(Resource.Success(body))
                } else {
                    emit(Resource.Error("Gagal menyimpan data: Respons kosong"))
                }
            } else {
                emit(Resource.Error("Error: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Terjadi kesalahan koneksi"))
        }
    }

    fun getActivityDetail(id: Long): Flow<Resource<BaseResponse<Kegiatan>>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.getKegiatanDetail(id)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    emit(Resource.Success(body))
                } else {
                    emit(Resource.Error("Gagal memuat detail kegiatan: Respons kosong"))
                }
            } else {
                emit(Resource.Error("Error: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Terjadi kesalahan koneksi"))
        }
    }

    fun updateActivity(id: Long, kegiatan: Kegiatan): Flow<Resource<BaseResponse<Kegiatan>>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.updateKegiatan(id, kegiatan)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    emit(Resource.Success(body))
                } else {
                    emit(Resource.Error("Gagal memperbarui kegiatan: Respons kosong"))
                }
            } else {
                emit(Resource.Error("Error: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Terjadi kesalahan koneksi"))
        }
    }
}
