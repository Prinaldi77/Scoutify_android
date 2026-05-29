package com.pab.scoutify.data.repository

import com.pab.scoutify.api.RetrofitClient
import com.pab.scoutify.model.BaseResponse
import com.pab.scoutify.model.LoginResponse
import com.pab.scoutify.model.request.LoginRequest
import com.pab.scoutify.model.request.RegisterRequest
import com.pab.scoutify.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository {

    private val apiService = RetrofitClient.instance

    suspend fun login(loginRequest: LoginRequest): Resource<LoginResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.login(loginRequest)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Resource.Success(body)
                } else {
                    Resource.Error("Response body is empty")
                }
            } else {
                val errorCode = response.code()
                val errorMsg = try {
                    val errorJson = response.errorBody()?.string()
                    if (errorJson != null) {
                        val jsonObject = org.json.JSONObject(errorJson)
                        jsonObject.getString("message")
                    } else {
                        "Login Gagal: Error $errorCode"
                    }
                } catch (e: Exception) {
                    if (errorCode == 500) "Server Error (500). Periksa backend Anda." else "Login Gagal: Error $errorCode"
                }
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Terjadi kesalahan jaringan")
        }
    }

    suspend fun register(registerRequest: RegisterRequest): Resource<BaseResponse<Any>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.register(registerRequest)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Resource.Success(body)
                } else {
                    Resource.Error("Response body is empty")
                }
            } else {
                Resource.Error("Pendaftaran Gagal: Error ${response.code()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Terjadi kesalahan jaringan")
        }
    }
}
