package com.example.dhvani.data

import com.example.dhvani.m.LoginRequest
import com.example.dhvani.model.AuthResponse
import com.example.dhvani.model.RegisterRequest
import com.example.dhvani.model.User
import retrofit2.Response
import retrofit2.http.*

interface AuthApi{
    @POST("users/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<User>

    @POST("users/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<AuthResponse>

    @GET("users/me")
    suspend fun getCurrentUser(
        @Header("Authorization") token: String
    ): Response<User>

    @POST("users/logout")
    suspend fun logout(): Response<Unit>

    @GET("users/refresh")
    suspend fun refreshToken(): Response<AuthResponse>
}