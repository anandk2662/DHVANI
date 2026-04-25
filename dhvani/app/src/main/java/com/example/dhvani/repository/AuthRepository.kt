package com.example.dhvani.repository

import android.content.Context
import com.example.dhvani.data.RetrofitInstance
import com.example.dhvani.data.TokenManager
import com.example.dhvani.m.LoginRequest
import com.example.dhvani.model.AuthResponse
import com.example.dhvani.model.RegisterRequest
import com.example.dhvani.model.User
import retrofit2.Response

class AuthRepository(private val context: Context){
    private val tokenManager = TokenManager(context)

    suspend fun login(email: String, password: String): AuthResponse? {
        val response = RetrofitInstance.getApi(context).login(LoginRequest(email, password))
        if (response.isSuccessful) {
            return response.body()
        }
        return null
    }

    suspend fun register(name: String, email: String, password: String): User? {
        val response = RetrofitInstance.getApi(context).register(RegisterRequest(name, email, password))
        if (response.isSuccessful) {
            return response.body()
        }
        return null
    }

    suspend fun refreshAccessToken(): String? {
        return try {
            val response = RetrofitInstance.getRefreshApi().refreshToken()
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                tokenManager.saveTokens(authResponse.accessToken, authResponse.refreshToken)
                authResponse.accessToken
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
