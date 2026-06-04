package com.pab.scoutify.data.repository

import android.util.Log
import com.pab.scoutify.api.RetrofitClient
import com.pab.scoutify.model.BaseResponse
import com.pab.scoutify.model.LoginResponse
import com.pab.scoutify.model.User
import com.pab.scoutify.model.request.LoginRequest
import com.pab.scoutify.model.request.RegisterRequest
import com.pab.scoutify.utils.Resource
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class AuthRepository {

    private val apiService = RetrofitClient.instance
    private val gson = Gson()

    suspend fun login(loginRequest: LoginRequest): Resource<LoginResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.login(loginRequest)
            
            if (response.isSuccessful) {
                val loginResponse = response.body()
                if (loginResponse != null) {
                    val token = loginResponse.anyToken
                    val user = loginResponse.anyUser
                    
                    if (!token.isNullOrEmpty()) {
                        Log.d("AUTH_DEBUG", "Login Berhasil, Token ditemukan: ${token.take(10)}...")
                        
                        // Normalisasi response agar field standar selalu terisi
                        val finalResponse = LoginResponse(
                            success = loginResponse.success ?: true,
                            message = loginResponse.message ?: "Login successful",
                            user = user,
                            tokens = com.pab.scoutify.model.Tokens(token, loginResponse.tokens?.refreshToken ?: loginResponse.data?.tokens?.refreshToken),
                            accessTokenDirect = token,
                            accessTokenSnake = token,
                            tokenSimple = token,
                            data = null
                        )
                        return@withContext Resource.Success(finalResponse)
                    } else {
                        Log.e("AUTH_DEBUG", "Pesan sukses tapi token tidak ditemukan. Response: $loginResponse")
                        return@withContext Resource.Error("Login berhasil tapi Token tidak ditemukan. Periksa backend.")
                    }
                } else {
                    return@withContext Resource.Error("Response body kosong")
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: ""
                Log.e("AUTH_DEBUG", "Login error: $errorBody")
                val message = try {
                    val errorMap = gson.fromJson(errorBody, Map::class.java)
                    errorMap["message"]?.toString() 
                        ?: errorMap["msg"]?.toString()
                        ?: "Login Gagal (Status: ${response.code()})"
                } catch (e: Exception) {
                    "Login Gagal (Status: ${response.code()})"
                }
                return@withContext Resource.Error(message)
            }
        } catch (e: Exception) {
            Log.e("AUTH_DEBUG", "Login Exception", e)
            Resource.Error("Kesalahan: ${e.localizedMessage}")
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
