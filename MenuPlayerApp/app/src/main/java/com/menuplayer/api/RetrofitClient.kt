package com.menuplayer.api

import com.menuplayer.utils.PrefsManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private var _api: ApiService? = null

    val api: ApiService
        get() = _api ?: buildApi().also { _api = it }

    /**
     * Call this whenever the base URL or token changes (e.g. after Settings save).
     */
    fun invalidate() {
        _api = null
    }

    private fun buildApi(): ApiService {
        val baseUrl = PrefsManager.serverUrl.trimEnd('/') + "/"

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val authInterceptor = Interceptor { chain ->
            val original = chain.request()
            val token = PrefsManager.apiToken
            val request = if (token.isNotBlank()) {
                original.newBuilder()
                    .header("Authorization", "token $token")
                    .build()
            } else original
            chain.proceed(request)
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)          // remove in release builds if desired
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
