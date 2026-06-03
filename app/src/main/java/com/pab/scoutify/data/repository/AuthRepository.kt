package com.pab.scoutify.data.repository

import android.util.Log
import com.pab.scoutify.api.RetrofitClient
import com.pab.scoutify.model.BaseResponse
import com.pab.scoutify.model.LoginResponse
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
            
            // 1. Ambil JSON mentah (Hanya baca sekali agar tidak error)
            val rawJson = if (response.isSuccessful) {
                gson.toJson(response.body())
            } else {
                response.errorBody()?.string() ?: ""
            }

            Log.d("AUTH_DEBUG", "Raw JSON Response: $rawJson")

            if (rawJson.isEmpty() || rawJson == "null") {
                return@withContext Resource.Error("Server tidak memberikan respon (Status: ${response.code()})")
            }

            val jsonObject = try { JSONObject(rawJson) } catch (e: Exception) { null }
            
            // 2. Cari pesan sukses/gagal di berbagai field
            val message = jsonObject?.let {
                it.optString("message", "")
                    .ifEmpty { it.optString("msg", "") }
                    .ifEmpty { it.optJSONObject("data")?.optString("message", "") ?: "" }
            } ?: ""

            // 3. Tentukan apakah ini sukses (berdasarkan HTTP status ATAU isi pesan)
            val isStatusSuccess = response.isSuccessful || 
                                jsonObject?.optBoolean("success") == true || 
                                jsonObject?.optBoolean("status") == true
                                
            val isMessageSuccess = message.lowercase().let { 
                it.contains("succes") || it.contains("berhasil") 
            } || rawJson.lowercase().contains("succes")

            if (isStatusSuccess || isMessageSuccess) {
                // 4. Parsing data user
                val dataObj = jsonObject?.optJSONObject("data")
                val loginData: LoginResponse? = try {
                    if (dataObj != null) gson.fromJson(dataObj.toString(), LoginResponse::class.java)
                    else gson.fromJson(rawJson, LoginResponse::class.java)
                } catch (e: Exception) { null }

                // 5. Cari Token sekuat tenaga di semua kemungkinan lokasi
                val token = loginData?.anyToken ?: 
                            jsonObject?.optString("token", "")?.takeIf { it.isNotEmpty() } ?:
                            jsonObject?.optString("accessToken", "")?.takeIf { it.isNotEmpty() } ?:
                            dataObj?.optString("token", "")?.takeIf { it.isNotEmpty() } ?:
                            jsonObject?.optJSONObject("tokens")?.optString("accessToken")

                if (!token.isNullOrEmpty()) {
                    Log.d("AUTH_DEBUG", "Login Berhasil, Token ditemukan.")
                    val finalResponse = LoginResponse(
                        success = true,
                        message = message,
                        user = loginData?.user,
                        tokens = com.pab.scoutify.model.Tokens(token, null),
                        accessTokenDirect = token,
                        accessTokenSnake = null,
                        tokenSimple = null
                    )
                    return@withContext Resource.Success(finalResponse)
                } else {
                    Log.e("AUTH_DEBUG", "Pesan sukses tapi token tidak ditemukan.")
                    return@withContext Resource.Error("Login berhasil tapi Token tidak ditemukan. Periksa backend.")
                }
            }

            // 6. Jika tidak ada tanda sukses, kembalikan pesan error
            val errorMsg = if (message.isNotEmpty()) message else "Login Gagal (Status: ${response.code()})"
            Resource.Error(errorMsg)

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
