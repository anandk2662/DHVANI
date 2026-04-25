package com.example.dhvani.data

import android.content.Context
import com.example.dhvani.repository.AuthRepository
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class TokenInterceptor(context: Context) : Interceptor {
    private val tokenManager = TokenManager(context)
    // Use a lazy repository to avoid circular dependency
    private val repository by lazy { AuthRepository(context) }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val accessToken = tokenManager.getAccessToken()

        val requestBuilder = originalRequest.newBuilder()
        if (accessToken != null) {
            requestBuilder.header("Authorization", "Bearer $accessToken")
        }

        val response = chain.proceed(requestBuilder.build())

        if (response.code == 401) {
            val refreshToken = tokenManager.getRefreshToken()
            if (refreshToken != null) {
                synchronized(this) {
                    // Try to refresh the token using the repository
                    val newAccessToken = runBlocking {
                        repository.refreshAccessToken()
                    }

                    if (newAccessToken != null) {
                        response.close()
                        val newRequest = originalRequest.newBuilder()
                            .header("Authorization", "Bearer $newAccessToken")
                            .build()
                        return chain.proceed(newRequest)
                    }
                }
            }
        }

        return response
    }
}