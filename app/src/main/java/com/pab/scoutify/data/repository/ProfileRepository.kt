package com.pab.scoutify.data.repository

import com.pab.scoutify.api.ProfileApiService
import com.pab.scoutify.model.*
import com.pab.scoutify.utils.Resource
import okhttp3.MultipartBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor(
    private val apiService: ProfileApiService
) {
    suspend fun getProfile(): Resource<ProfileData> {
        return try {
            val response = apiService.getProfile()
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Gagal mengambil data profil")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Terjadi kesalahan")
        }
    }

    suspend fun updateProfile(request: UpdateProfileRequest): Resource<ProfileData> {
        return try {
            val response = apiService.updateProfile(request)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!.data)
            } else {
                Resource.Error(response.body()?.message ?: "Gagal memperbarui profil")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Terjadi kesalahan")
        }
    }

    suspend fun uploadPhoto(photo: MultipartBody.Part): Resource<ProfileData> {
        return try {
            val response = apiService.uploadPhoto(photo)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!.data)
            } else {
                Resource.Error(response.body()?.message ?: "Gagal mengunggah foto")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Terjadi kesalahan")
        }
    }

    suspend fun changePassword(request: ChangePasswordRequest): Resource<Any> {
        return try {
            val response = apiService.changePassword(request)
            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                Resource.Error("Gagal mengubah kata sandi")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Terjadi kesalahan")
        }
    }

    suspend fun logout(): Resource<Any> {
        return try {
            val response = apiService.logout()
            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                Resource.Error("Gagal logout")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Terjadi kesalahan")
        }
    }
}
