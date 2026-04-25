package com.example.dhvani.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class TokenManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    fun saveTokens(accessToken: String?, refreshToken: String?) {
        prefs.edit {
            if (accessToken != null) {
                putString("access_token", accessToken)
            }
            if (refreshToken != null) {
                putString("refresh_token", refreshToken)
            }
        }
    }

    fun getAccessToken(): String? {
        return prefs.getString("access_token", null)
    }

    fun getRefreshToken(): String? {
        return prefs.getString("refresh_token", null)
    }

    fun clearTokens() {
        prefs.edit {
            remove("access_token")
            remove("refresh_token")
        }
    }
}