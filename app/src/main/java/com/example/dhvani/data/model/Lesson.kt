package com.example.dhvani.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Lesson(
    val id: String,
    val title: String,
    val signs: List<SignItem>,
    val steps: List<LessonStep>,
    val category: SignCategory,
    val order: Int,
    val isLocked: Boolean = true,
    val status: LessonStatus = LessonStatus.LOCKED
)

@Serializable
data class MatchPair(
    val sign: SignItem,
    val label: String
)

@Serializable
sealed class LessonStep {
    @Serializable
    data class Learn(val sign: SignItem) : LessonStep()
    
    @Serializable
    data class Quiz(
        val sign: SignItem,
        val options: List<SignItem>
    ) : LessonStep()
    
    @Serializable
    data class Match(
        val pairs: List<MatchPair>
    ) : LessonStep()

    @Serializable
    data class Camera(
        val targetSign: SignItem
    ) : LessonStep()
}

enum class LessonStatus {
    LOCKED,
    AVAILABLE,
    IN_PROGRESS,
    COMPLETED
}

@Serializable
data class UserLessonProgress(
    val lessonId: String,
    val status: LessonStatus,
    val completedAt: String? = null,
    val accuracy: Float = 0f
)

@Serializable
data class SignMastery(
    val signId: String,
    val masteryScore: Int = 0, // 0 to 100
    val lastPracticed: String? = null,
    val reviewPriority: Int = 0
)
