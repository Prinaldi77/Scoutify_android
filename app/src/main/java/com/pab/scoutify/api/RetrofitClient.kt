package com.pab.scoutify.api

import android.content.Context
import com.pab.scoutify.BuildConfig
import com.pab.scoutify.ScoutifyApplication
import com.pab.scoutify.ui.auth.SessionManager
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private val BASE_URL = BuildConfig.BASE_URL

    var authToken: String? = null
    private var retrofit: Retrofit? = null

    val instance: ApiService by lazy {
        getInstance(ScoutifyApplication.appContext)
    }

    fun getInstance(context: Context): ApiService {
        return getRetrofit(context).create(ApiService::class.java)
    }

    fun getTempInstance(): ApiService {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    @Synchronized
    private fun getRetrofit(context: Context): Retrofit {
        if (retrofit == null) {
            val sessionManager = SessionManager(context)
            if (authToken == null) {
                authToken = sessionManager.fetchAuthToken()
            }

            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(logging)
                .addInterceptor { chain ->
                    val original = chain.request()
                    val requestBuilder = original.newBuilder()
                    
                    val token = authToken ?: sessionManager.fetchAuthToken()
                    token?.let {
                        requestBuilder.addHeader("Authorization", "Bearer $it")
                    }
                    
                    chain.proceed(requestBuilder.build())
                }
                .authenticator(TokenAuthenticator(context.applicationContext))
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()

            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!
    }
}
