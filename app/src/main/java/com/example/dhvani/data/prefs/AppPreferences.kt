package com.example.dhvani.data.prefs

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("dhvani_prefs", Context.MODE_PRIVATE)

    var aiModelUrl: String
        get() = prefs.getString(KEY_AI_URL, DEFAULT_URL) ?: DEFAULT_URL
        set(value) = prefs.edit().putString(KEY_AI_URL, value).apply()

    var completedLessons: Set<String>
        get() = prefs.getStringSet(KEY_COMPLETED_LESSONS, emptySet())?.toSet() ?: emptySet()
        set(value) = prefs.edit().putStringSet(KEY_COMPLETED_LESSONS, value).apply()

    companion object {
        private const val KEY_AI_URL = "ai_model_url"
        private const val KEY_COMPLETED_LESSONS = "completed_lessons"
        private const val DEFAULT_URL = "https://api-inference.huggingface.co/models/google/mediapipe-hand-landmarks"
    }
}
