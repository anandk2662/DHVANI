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
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.serializer
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
    val league_index: Int = 0,
    val friend_ids: List<String>? = emptyList(),
    val shared_streaks: Map<String, Int>? = emptyMap()
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
        val fetched = getProfileById(userId) ?: return null
        
        val current = _currentUserProfile.value
        if (current != null && current.id == fetched.id) {
            // Merge logic: protect local progress from old or uninitialized DB state
            val merged = fetched.copy(
                xp_points = maxOf(fetched.xp_points, current.xp_points),
                current_streak = maxOf(fetched.current_streak, current.current_streak),
                login_streak = maxOf(fetched.login_streak, current.login_streak),
                last_active_date = if (current.last_active_date != null && 
                    (fetched.last_active_date == null || current.last_active_date > fetched.last_active_date!!))
                    current.last_active_date else fetched.last_active_date,
                friend_ids = if (!fetched.friend_ids.isNullOrEmpty()) fetched.friend_ids else current.friend_ids,
                shared_streaks = if (!fetched.shared_streaks.isNullOrEmpty()) fetched.shared_streaks else current.shared_streaks
            )
            _currentUserProfile.value = merged
            return merged
        } else {
            _currentUserProfile.value = fetched
            return fetched
        }
    }

    suspend fun getProfileById(userId: String): UserProfile? {
        return try {
            supabaseClient.postgrest["profiles"]
                .select { filter { eq("id", userId) } }
                .decodeSingleOrNull<UserProfile>()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getProfilesByIds(userIds: List<String>): List<UserProfile> {
        if (userIds.isEmpty()) return emptyList()
        return try {
            supabaseClient.postgrest["profiles"]
                .select { 
                    filter { 
                        // Using a simple eq loop ifisIn is not resolving, 
                        // but usually it's isIn for Supabase-kt
                        or {
                            userIds.forEach { id -> eq("id", id) }
                        }
                    } 
                }
                .decodeList<UserProfile>()
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Failed to fetch multiple profiles", e)
            emptyList()
        }
    }

    suspend fun updateProfile(profile: UserProfile) {
        val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return
        try {
            // Using upsert for better reliability in syncing profile data
            supabaseClient.postgrest["profiles"].upsert(profile)
            _currentUserProfile.value = profile
            android.util.Log.d("AuthRepository", "Profile synced successfully for $userId")
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Failed to sync profile", e)
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
        
        _currentUserProfile.value = updatedProfile
        
        try {
            supabaseClient.postgrest["profiles"].upsert(updatedProfile)
            android.util.Log.d("AuthRepository", "XP updated in DB for ${profile.id}")
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Failed to update XP in DB: ${e.message}")
        }
    }

    suspend fun updateStreak(streak: Int, lastActiveDate: String? = null) {
        val profile = _currentUserProfile.value ?: return
        
        // login_streak (total active days) increments only if this is the first activity of the day
        val isFirstActivityEver = profile.last_active_date == null
        val isNewDay = lastActiveDate != null && lastActiveDate != profile.last_active_date
        
        val newLoginStreak = if ((isFirstActivityEver || isNewDay) && streak > 0) {
            profile.login_streak + 1
        } else {
            profile.login_streak
        }
        
        val updatedProfile = profile.copy(
            current_streak = streak,
            last_active_date = lastActiveDate ?: profile.last_active_date,
            login_streak = newLoginStreak
        )
        
        _currentUserProfile.value = updatedProfile
        
        try {
            val updateData = buildJsonObject {
                put("current_streak", streak)
                if (lastActiveDate != null) put("last_active_date", lastActiveDate)
                put("login_streak", newLoginStreak)
            }
            // Use update for better reliability on existing rows
            supabaseClient.postgrest["profiles"].update(updateData) {
                filter { eq("id", profile.id) }
            }
            android.util.Log.d("AuthRepository", "Streak updated in DB. Current: $streak, Total Days: $newLoginStreak")
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Failed to update streak in DB: ${e.message}")
        }
    }

    suspend fun getLeaderboard(leagueId: Int? = null): List<UserProfile> {
        return try {
            // Fetch top 100 users globally. Filtering by league is handled 
            // in the UI/ViewModel to ensure it works even if league_index column is missing.
            supabaseClient.postgrest["profiles"]
                .select {
                    order("xp_points", Order.DESCENDING)
                    limit(100)
                }
                .decodeList<UserProfile>()
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Failed to fetch leaderboard", e)
            emptyList()
        }
    }

    // --- Social Features ---

    suspend fun addFriend(friendId: String) {
        val currentProfile = _currentUserProfile.value ?: return
        val currentFriends = currentProfile.friend_ids?.toMutableList() ?: mutableListOf()
        
        if (!currentFriends.contains(friendId)) {
            currentFriends.add(friendId)
            val updatedProfile = currentProfile.copy(friend_ids = currentFriends)
            _currentUserProfile.value = updatedProfile
            
            try {
                supabaseClient.postgrest["profiles"].upsert(updatedProfile)
                android.util.Log.d("AuthRepository", "Friend added in DB")
            } catch (e: Exception) {
                android.util.Log.e("AuthRepository", "Failed to add friend in DB: ${e.message}")
            }
        }
    }

    suspend fun updateSharedStreak(friendId: String, increment: Boolean = true) {
        val currentProfile = _currentUserProfile.value ?: return
        val shared = currentProfile.shared_streaks?.toMutableMap() ?: mutableMapOf()
        
        val currentVal = shared[friendId] ?: 0
        shared[friendId] = if (increment) currentVal + 1 else 0
        
        val updatedProfile = currentProfile.copy(shared_streaks = shared)
        _currentUserProfile.value = updatedProfile
        
        try {
            supabaseClient.postgrest["profiles"].upsert(updatedProfile)
            android.util.Log.d("AuthRepository", "Shared streak updated in DB")
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Failed to update shared streak in DB: ${e.message}")
        }
    }
}
