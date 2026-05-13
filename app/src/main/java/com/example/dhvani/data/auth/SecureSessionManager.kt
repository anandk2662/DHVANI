package com.example.dhvani.data.auth

import android.content.Context
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import io.github.jan.supabase.auth.SessionManager
import io.github.jan.supabase.auth.user.UserSession
import kotlinx.serialization.json.Json
import javax.inject.Inject

class SecureSessionManager @Inject constructor(
    context: Context
) : SessionManager {

    private val masterKey = MasterKey.Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secure_auth_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun saveSession(session: UserSession) {
        val sessionJson = json.encodeToString(UserSession.serializer(), session)
        sharedPreferences.edit {
            putString(KEY_SESSION, sessionJson)
        }
    }

    override suspend fun loadSession(): UserSession? {
        val sessionJson = sharedPreferences.getString(KEY_SESSION, null) ?: return null
        return try {
            json.decodeFromString(UserSession.serializer(), sessionJson)
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun deleteSession() {
        sharedPreferences.edit {
            remove(KEY_SESSION)
        }
    }

    companion object {
        private const val KEY_SESSION = "supabase_session"
    }
}
