package com.example.dhvani.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class UserProfile(
    val id: String,
    val username: String? = null,
    val full_name: String? = null,
    val avatar_url: String? = null,
    val region: String? = null,
    val xp_points: Int = 0,
    val current_streak: Int = 0,
    val last_active_date: String? = null,
    val login_streak: Int = 0,
    val created_at: String? = null,
    val league_index: Int = 0, // 0 to 9 for 10 leagues
    val friend_ids: List<String> = emptyList(),
    val shared_streaks: Map<String, Int> = emptyMap() // Map<FriendId, StreakCount>
)

@Singleton
class AuthRepository @Inject constructor(
    private val supabaseClient: SupabaseClient
) {
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _currentUserProfile = MutableStateFlow<UserProfile?>(null)
    val currentUserProfile = _currentUserProfile.asStateFlow()

    private val _sessionStatus = MutableStateFlow<SessionStatus>(SessionStatus.NotAuthenticated(false))
    val sessionStatus = _sessionStatus.asStateFlow()

    init {
        repositoryScope.launch {
            supabaseClient.auth.sessionStatus.collectLatest { status ->
                _sessionStatus.value = status
                if (status is SessionStatus.Authenticated) {
                    getProfile()
                } else {
                    _currentUserProfile.value = null
                }
            }
        }
    }

    // --- Authentication ---

    suspend fun signUp(email: String, password: String) {
        val user = supabaseClient.auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
        
        if (user?.identities.isNullOrEmpty()) {
            throw UserAlreadyExistsException()
        }
    }

    class UserAlreadyExistsException : Exception("A user with this email already exists. Please log in.")

    suspend fun resendOtp(email: String) {
        supabaseClient.auth.resendEmail(
            type = OtpType.Email.SIGNUP,
            email = email
        )
    }

    suspend fun verifyEmailOtp(email: String, token: String) {
        supabaseClient.auth.verifyEmailOtp(
            type = OtpType.Email.SIGNUP,
            email = email,
            token = token
        )
    }

    suspend fun createInitialProfile(
        userId: String,
        username: String,
        fullName: String,
        region: String
    ) {
        try {
            val profile = UserProfile(
                id = userId,
                username = username,
                full_name = fullName,
                region = region
            )
            supabaseClient.postgrest["profiles"].upsert(profile)
            _currentUserProfile.value = profile
            android.util.Log.d("AuthRepository", "Profile created successfully for $userId")
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Failed to create profile", e)
            throw e
        }
    }

    suspend fun signIn(email: String, password: String) {
        supabaseClient.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    suspend fun signInWithGoogle() {
        supabaseClient.auth.signInWith(Google, redirectUrl = "com.example.dhvani://login-callback")
    }

    suspend fun signInWithIdToken(idToken: String) {
        supabaseClient.auth.signInWith(io.github.jan.supabase.auth.providers.builtin.IDToken) {
            this.idToken = idToken
            this.provider = Google
        }
    }

    suspend fun signOut() {
        supabaseClient.auth.signOut()
        _currentUserProfile.value = null
    }

    fun isUserLoggedIn(): Boolean = supabaseClient.auth.currentUserOrNull() != null
    
    fun getCurrentUser(): UserInfo? = supabaseClient.auth.currentUserOrNull()
    
    fun getCurrentSession() = supabaseClient.auth.currentSessionOrNull()

    // --- Profile Management ---

    suspend fun getProfile(): UserProfile? {
        val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return null
        return try {
            val profile = supabaseClient.postgrest["profiles"]
                .select { filter { eq("id", userId) } }
                .decodeSingleOrNull<UserProfile>()
            
            _currentUserProfile.value = profile
            profile
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateProfile(profile: UserProfile) {
        val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return
        try {
            supabaseClient.postgrest["profiles"].update(profile) {
                filter { eq("id", userId) }
            }
            _currentUserProfile.value = profile
            android.util.Log.d("AuthRepository", "Profile updated successfully for $userId")
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Failed to update profile", e)
            throw e
        }
    }

    suspend fun addXp(points: Int) {
        val profile = _currentUserProfile.value ?: return
        val newXp = profile.xp_points + points
        val newLeague = com.example.dhvani.data.model.League.getByXp(newXp)
        
        val updatedProfile = profile.copy(
            xp_points = newXp,
            league_index = newLeague.id
        )
        
        // Update local state immediately
        _currentUserProfile.value = updatedProfile
        
        try {
            updateProfile(updatedProfile)
            android.util.Log.d("AuthRepository", "XP and League updated in DB for ${profile.id}")
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Failed to update XP in DB: ${e.message}")
        }
    }

    suspend fun updateStreak(streak: Int, lastActiveDate: String? = null) {
        val profile = _currentUserProfile.value ?: return
        val updatedProfile = profile.copy(
            current_streak = streak,
            last_active_date = lastActiveDate,
            login_streak = if (streak > profile.login_streak) streak else profile.login_streak
        )
        
        // Update local state immediately so UI reflects it even if DB fails
        _currentUserProfile.value = updatedProfile
        
        try {
            supabaseClient.postgrest["profiles"].update(updatedProfile) {
                filter { eq("id", profile.id) }
            }
            android.util.Log.d("AuthRepository", "Streak updated successfully for ${profile.id}")
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Failed to update streak in DB: ${e.message}")
        }
    }

    suspend fun getLeaderboard(): List<UserProfile> {
        return try {
            supabaseClient.postgrest["profiles"]
                .select {
                    order("xp_points", Order.DESCENDING)
                    limit(20)
                }
                .decodeList<UserProfile>()
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Failed to fetch leaderboard", e)
            emptyList()
        }
    }
}
