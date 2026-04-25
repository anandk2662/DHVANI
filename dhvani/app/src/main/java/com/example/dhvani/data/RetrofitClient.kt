package com.example.dhvani.data

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    private const val BASE_URL = "http://10.0.2.2:5000/api/"

    private var apiInstance: AuthApi? = null
    private var refreshApiInstance: AuthApi? = null

    // Common logging interceptor shared by both clients
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Common Retrofit Builder to avoid duplication
    private fun createRetrofitBuilder() = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())

    /**
     * Main API instance with TokenInterceptor for authenticated requests.
     */
    fun getApi(context: Context): AuthApi {
        return apiInstance ?: synchronized(this) {
            val client = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .addInterceptor(TokenInterceptor(context.applicationContext))
                .build()

            val instance = createRetrofitBuilder()
                .client(client)
                .build()
                .create(AuthApi::class.java)
            
            apiInstance = instance
            instance
        }
    }

    /**
     * "Clean" API instance without TokenInterceptor. 
     * Used for login, register, and token refresh to avoid circular dependencies.
     */
    fun getRefreshApi(): AuthApi {
        return refreshApiInstance ?: synchronized(this) {
            val client = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build()

            val instance = createRetrofitBuilder()
                .client(client)
                .build()
                .create(AuthApi::class.java)

            refreshApiInstance = instance
            instance
        }
    }
}