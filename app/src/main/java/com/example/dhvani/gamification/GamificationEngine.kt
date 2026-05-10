package com.example.dhvani.gamification

import com.example.dhvani.data.repository.AuthRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GamificationEngine @Inject constructor(
    private val authRepository: AuthRepository
) {
    companion object {
        const val XP_PER_CORRECT_ANSWER = 10
        const val XP_PERFECT_QUIZ_BONUS = 50
        const val XP_STREAK_BONUS = 25
        const val XP_LESSON_COMPLETION = 100
        private val DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE
    }

    suspend fun onCorrectAnswer() {
        authRepository.addXp(XP_PER_CORRECT_ANSWER)
    }

    suspend fun onLessonCompleted(perfect: Boolean) {
        var totalXp = XP_LESSON_COMPLETION
        if (perfect) totalXp += XP_PERFECT_QUIZ_BONUS
        authRepository.addXp(totalXp)
        updateStreak()
    }

    suspend fun onQuizCompleted(perfect: Boolean) {
        var totalXp = 50 // Base XP for quiz
        if (perfect) totalXp += XP_PERFECT_QUIZ_BONUS
        authRepository.addXp(totalXp)
        updateStreak()
    }

    private suspend fun updateStreak() {
        val profile = authRepository.currentUserProfile.value ?: return
        val today = LocalDate.now()
        val todayString = today.format(DATE_FORMATTER)
        
        val lastActiveString = profile.last_active_date
        
        if (lastActiveString == null) {
            // First time activity
            authRepository.updateStreak(1, todayString)
            return
        }

        val lastActiveDate = LocalDate.parse(lastActiveString, DATE_FORMATTER)
        val daysBetween = ChronoUnit.DAYS.between(lastActiveDate, today)

        when {
            daysBetween == 0L -> {
                // Already active today, streak stays the same
                android.util.Log.d("Streak", "Already active today")
            }
            daysBetween == 1L -> {
                // Consecutive day!
                authRepository.updateStreak(profile.current_streak + 1, todayString)
                android.util.Log.d("Streak", "Streak increased to ${profile.current_streak + 1}")
            }
            else -> {
                // Missed one or more days, reset to 1
                authRepository.updateStreak(1, todayString)
                android.util.Log.d("Streak", "Streak reset due to $daysBetween days gap")
            }
        }
    }

    // Called on app launch to check if streak was broken yesterday
    suspend fun checkAndResetBrokenStreak() {
        val profile = authRepository.currentUserProfile.value ?: return
        val lastActiveString = profile.last_active_date ?: return
        
        val today = LocalDate.now()
        val lastActiveDate = LocalDate.parse(lastActiveString, DATE_FORMATTER)
        val daysBetween = ChronoUnit.DAYS.between(lastActiveDate, today)

        if (daysBetween > 1L) {
            // Broken streak
            authRepository.updateStreak(0, lastActiveString) // Keep last active, but reset count
            android.util.Log.d("Streak", "Streak broken at launch")
        }
    }

    fun calculateLevel(totalXp: Int): Int {
        return (totalXp / 1000) + 1
    }

    fun getXpForNextLevel(currentLevel: Int): Int {
        return currentLevel * 1000
    }
}
