package com.pab.scoutify

import org.json.JSONObject
import com.google.gson.Gson
import com.pab.scoutify.model.LoginResponse
import org.junit.Test
import org.junit.Assert.*

class ExampleUnitTest {
    @Test
    fun testTokenParsing() {
        val originalJson = """
        {"success":true,"message":"Login successful","tokens":{"accessToken":"test_token_123","refreshToken":"uh7jxv7fbc3y"},"accessToken":"test_token_123","access_token":"test_token_123","refresh_token":"uh7jxv7fbc3y","user":{"id":"1f3e0684-04ea-4604-b36b-b6361ee32690","name":"Kak Budi","email":"siswa@scoutify.com","role":"SISWA","avatar":null,"regu":"Regu Rajawali","rank":null,"gugus_depan":"Ambalan Soekarno"},"data":{"access_token":"test_token_123","refresh_token":"uh7jxv7fbc3y","user":{"id":"1f3e0684-04ea-4604-b36b-b6361ee32690","name":"Kak Budi","email":"siswa@scoutify.com","role":"SISWA","avatar":null,"regu":"Regu Rajawali","rank":null,"gugus_depan":"Ambalan Soekarno"}}}
        """.trimIndent()

        val gson = Gson()
        // Simulate what Retrofit does when the response type is Any: it deserializes JSON into a Map
        val mapType = object : com.google.gson.reflect.TypeToken<Any>() {}.type
        val bodyAsAny: Any = gson.fromJson(originalJson, mapType)

        // Simulate AuthRepository: gson.toJson(response.body())
        val rawJson = gson.toJson(bodyAsAny)
        println("rawJson: $rawJson")

        val jsonObject = try { JSONObject(rawJson) } catch (e: Exception) { null }
        assertNotNull("JSONObject should not be null", jsonObject)

        val message = jsonObject?.let {
            it.optString("message", "")
                .ifEmpty { it.optString("msg", "") }
                .ifEmpty { it.optJSONObject("data")?.optString("message", "") ?: "" }
        } ?: ""
        assertEquals("Login successful", message)

        val dataObj = jsonObject?.optJSONObject("data")
        val loginData: LoginResponse? = try {
            if (dataObj != null) gson.fromJson(dataObj.toString(), LoginResponse::class.java)
            else gson.fromJson(rawJson, LoginResponse::class.java)
        } catch (e: Exception) { null }

        assertNotNull("loginData should not be null", loginData)

        val token = jsonObject?.optString("accessToken", "")?.takeIf { it.isNotEmpty() } ?:
                    jsonObject?.optString("access_token", "")?.takeIf { it.isNotEmpty() } ?:
                    jsonObject?.optString("token", "")?.takeIf { it.isNotEmpty() } ?:
                    jsonObject?.optJSONObject("tokens")?.optString("accessToken", "")?.takeIf { it.isNotEmpty() } ?:
                    jsonObject?.optJSONObject("tokens")?.optString("access_token", "")?.takeIf { it.isNotEmpty() } ?:
                    dataObj?.optString("accessToken", "")?.takeIf { it.isNotEmpty() } ?:
                    dataObj?.optString("access_token", "")?.takeIf { it.isNotEmpty() } ?:
                    dataObj?.optString("token", "")?.takeIf { it.isNotEmpty() } ?:
                    loginData?.anyToken

        println("Extracted token: $token")
        assertEquals("test_token_123", token)
    }
}