package com.example.dhvani.ml

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface RemoteInferenceService {
    @POST("sign")
    suspend fun predictSign(@Body request: SignInferenceRequest): Response<SignInferenceResponse>
}
