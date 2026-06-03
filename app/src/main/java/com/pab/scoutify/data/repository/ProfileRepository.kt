package com.pab.scoutify.data.repository

import com.pab.scoutify.api.ProfileApiService
import com.pab.scoutify.model.BaseResponse
import com.pab.scoutify.model.ChangePasswordRequest
import com.pab.scoutify.model.ProfileData
import com.pab.scoutify.model.request.UpdateProfileRequest
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
            val body = response.body()
            if (response.isSuccessful && body != null && body.success) {
                Resource.Success(body.data)
            } else {
                Resource.Error(body?.message ?: "Gagal mengambil data profil")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Terjadi kesalahan")
        }
    }

    suspend fun updateProfile(request: UpdateProfileRequest): Resource<ProfileData> {
        return try {
            val response = apiService.updateProfile(request)
            val body = response.body()
            if (response.isSuccessful && body != null && body.success) {
                Resource.Success(body.data)
            } else {
                Resource.Error(body?.message ?: "Gagal memperbarui profil")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Terjadi kesalahan")
        }
    }

    suspend fun uploadPhoto(photo: MultipartBody.Part): Resource<ProfileData> {
        return try {
            val response = apiService.uploadPhoto(photo)
            val body = response.body()
            if (response.isSuccessful && body != null && body.success) {
                Resource.Success(body.data)
            } else {
                Resource.Error(body?.message ?: "Gagal mengunggah foto")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Terjadi kesalahan")
        }
    }

    suspend fun changePassword(request: ChangePasswordRequest): Resource<Any> {
        return try {
            val response = apiService.changePassword(request)
            val body = response.body()
            if (response.isSuccessful && body != null && body.success) {
                Resource.Success(body.data ?: Unit)
            } else {
                Resource.Error(body?.message ?: "Gagal mengubah kata sandi")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Terjadi kesalahan")
        }
    }

    suspend fun logout(): Resource<Any> {
        return try {
            val response = apiService.logout()
            val body = response.body()
            if (response.isSuccessful && body != null && body.success) {
                Resource.Success(body.data ?: Unit)
            } else {
                Resource.Error(body?.message ?: "Gagal logout")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Terjadi kesalahan")
        }
    }
}
