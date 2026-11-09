package com.iqbal.pemetaanpeminatan.data

import android.content.Context
import com.iqbal.pemetaanpeminatan.SessionManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiConfig {
    private const val BASE_URL = "http://10.0.2.2:8000/" // Ganti IP server FastAPI jika production

    fun getApiService(context: Context? = null, onUnauthorized: (() -> Unit)? = null): ApiService {
        val clientBuilder = OkHttpClient.Builder()

        if (context != null) {
            val sessionManager = SessionManager(context)
            val authInterceptor = Interceptor { chain ->
                val originalRequest = chain.request()
                val token = sessionManager.fetchAuthToken()

                val requestBuilder = originalRequest.newBuilder()
                if (!token.isNullOrEmpty()) {
                    requestBuilder.addHeader("Authorization", "Bearer $token")
                }

                val response = chain.proceed(requestBuilder.build())

                if (response.code == 401) {
                    sessionManager.clearSession()
                    onUnauthorized?.invoke()
                }

                response
            }
            clientBuilder.addInterceptor(authInterceptor)
        }

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = clientBuilder
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

        return retrofit.create(ApiService::class.java)
    }
}
