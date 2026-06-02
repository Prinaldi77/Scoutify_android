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
class ActivityManagementRepository @Inject constructor(
    private val apiService: ApiService
) {
    fun getManagementActivities(status: String): Flow<Resource<List<Kegiatan>>> = flow {
        emit(Resource.Loading)
        try {
            // Using existing getKegiatan and filtering locally for now, 
            // but normally would use @GET("management/activities") with query filter
            val response = apiService.getKegiatan()
            if (response.isSuccessful) {
                val data = response.body()?.data ?: emptyList()
                // Filtering based on status (Aktif/Selesai)
                val filtered = if (status == "Aktif") {
                    data.filter { it.kategori?.contains("Aktif", ignoreCase = true) == true || it.kategori?.contains("Mendatang", ignoreCase = true) == true }
                } else {
                    data.filter { it.kategori?.contains("Selesai", ignoreCase = true) == true }
                }
                emit(Resource.Success(filtered))
            } else {
                emit(Resource.Error("Gagal memuat kegiatan: ${response.message()}"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Terjadi kesalahan koneksi"))
        }
    }

    suspend fun deleteActivity(id: Long): Resource<Any> {
        return try {
            val response = apiService.deleteKegiatan(id)
            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                Resource.Error("Gagal menghapus kegiatan")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Terjadi kesalahan")
        }
    }
}
