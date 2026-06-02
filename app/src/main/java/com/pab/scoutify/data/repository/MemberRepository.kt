package com.pab.scoutify.data.repository

import com.pab.scoutify.api.ApiService
import com.pab.scoutify.model.Anggota
import com.pab.scoutify.model.MemberDetail
import com.pab.scoutify.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MemberRepository @Inject constructor(
    private val apiService: ApiService
) {
    fun getMembers(): Flow<Resource<List<Anggota>>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.getAnggotas()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    emit(Resource.Success(body.data))
                } else {
                    emit(Resource.Error("Empty response body"))
                }
            } else {
                emit(Resource.Error("Error: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Unknown error"))
        }
    }

    fun getMemberDetail(id: Int): Flow<Resource<MemberDetail>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.getMemberDetail(id)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    emit(Resource.Success(body.data))
                } else {
                    emit(Resource.Error("Data detail tidak ditemukan"))
                }
            } else {
                emit(Resource.Error("Gagal memuat detail member"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Terjadi kesalahan"))
        }
    }

    fun createMember(anggota: Anggota): Flow<Resource<Anggota>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.createAnggota(anggota)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.data != null) {
                    emit(Resource.Success(body.data))
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

    fun updateMember(id: Int, anggota: Anggota): Flow<Resource<Anggota>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.updateAnggota(id, anggota)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.data != null) {
                    emit(Resource.Success(body.data))
                } else {
                    emit(Resource.Error("Gagal memperbarui data: Respons kosong"))
                }
            } else {
                emit(Resource.Error("Error: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Terjadi kesalahan koneksi"))
        }
    }

    suspend fun resetPassword(id: Int): Resource<Any> {
        return try {
            val response = apiService.resetMemberPassword(id)
            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                Resource.Error("Gagal mereset password")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Terjadi kesalahan")
        }
    }
}
