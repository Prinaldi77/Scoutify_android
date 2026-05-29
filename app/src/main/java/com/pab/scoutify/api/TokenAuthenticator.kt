package com.pab.scoutify.api

import android.content.Context
import com.pab.scoutify.model.request.RefreshTokenRequest
import com.pab.scoutify.ui.auth.SessionManager
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator(private val context: Context) : Authenticator {

    private val sessionManager = SessionManager(context)

    override fun authenticate(route: Route?, response: Response): Request? {
        // Limit request retry count to prevent infinite loop
        if (responseCount(response) >= 3) {
            return null
        }

        val refreshToken = sessionManager.fetchRefreshToken() ?: return null

        synchronized(this) {
            val currentToken = sessionManager.fetchAuthToken()
            
            // If the token was updated by another thread while we were waiting, retry with the new token
            val authorizationHeader = response.request.header("Authorization")
            if (authorizationHeader != "Bearer $currentToken") {
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $currentToken")
                    .build()
            }

            // Sync API call to get a new token
            val newTokens = runBlocking {
                try {
                    val tempService = RetrofitClient.getTempInstance()
                    val refreshResponse = tempService.refreshToken(RefreshTokenRequest(refreshToken))
                    if (refreshResponse.isSuccessful && refreshResponse.body()?.success == true) {
                        refreshResponse.body()?.data?.tokens
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    null
                }
            }

            if (newTokens != null) {
                // Update local storage
                sessionManager.saveAuthToken(newTokens.accessToken, newTokens.refreshToken)
                RetrofitClient.authToken = newTokens.accessToken

                // Retry request with new token
                return response.request.newBuilder()
                    .header("Authorization", "Bearer ${newTokens.accessToken}")
                    .build()
            } else {
                // Refresh failed: clear session
                sessionManager.clearSession()
                RetrofitClient.authToken = null
                return null
            }
        }
    }

    private fun responseCount(response: Response): Int {
        var result = 1
        var priorResponse = response.priorResponse
        while (priorResponse != null) {
            result++
            priorResponse = priorResponse.priorResponse
        }
        return result
    }
}
