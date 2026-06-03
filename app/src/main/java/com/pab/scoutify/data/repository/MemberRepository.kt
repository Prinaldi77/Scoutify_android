package com.pab.scoutify.data.repository

import com.pab.scoutify.api.ApiService
import com.pab.scoutify.model.*
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
            val body = response.body()
            if (response.isSuccessful && body != null && body.success) {
                emit(Resource.Success(body.data))
            } else {
                emit(Resource.Error(body?.message ?: "Gagal memuat daftar anggota"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Terjadi kesalahan"))
        }
    }

    fun getMemberDetail(id: Int): Flow<Resource<MemberDetail>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.getMemberDetail(id)
            val body = response.body()
            if (response.isSuccessful && body != null && body.success) {
                emit(Resource.Success(body.data))
            } else {
                emit(Resource.Error(body?.message ?: "Data detail tidak ditemukan"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Terjadi kesalahan"))
        }
    }

    fun createMember(anggota: Anggota): Flow<Resource<Anggota>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.createAnggota(anggota)
            val body = response.body()
            if (response.isSuccessful && body != null && body.success) {
                emit(Resource.Success(body.data))
            } else {
                emit(Resource.Error(body?.message ?: "Gagal menyimpan data"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Terjadi kesalahan koneksi"))
        }
    }

    fun updateMember(id: Int, anggota: Anggota): Flow<Resource<Anggota>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.updateAnggota(id, anggota)
            val body = response.body()
            if (response.isSuccessful && body != null && body.success) {
                emit(Resource.Success(body.data))
            } else {
                emit(Resource.Error(body?.message ?: "Gagal memperbarui data"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Terjadi kesalahan koneksi"))
        }
    }

    suspend fun resetPassword(id: Int): Resource<Any> {
        return try {
            val response = apiService.resetMemberPassword(id)
            val body = response.body()
            if (response.isSuccessful && body != null && body.success) {
                Resource.Success(body.data ?: Unit)
            } else {
                Resource.Error(body?.message ?: "Gagal mereset password")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Terjadi kesalahan")
        }
    }
}
