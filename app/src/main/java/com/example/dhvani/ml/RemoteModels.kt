package com.example.dhvani.ml

import kotlinx.serialization.Serializable

@Serializable
data class LandmarkContainer(
    val Left: List<List<Float>> = emptyList(),
    val Right: List<List<Float>> = emptyList()
)

@Serializable
data class SignInferenceRequest(
    val landmarks: LandmarkContainer
)

@Serializable
data class PredictionData(
    val sign: String,
    val confidence: Float
)

@Serializable
data class SignInferenceResponse(
    val status: Int,
    val message: String,
    val data: PredictionData? = null
)
