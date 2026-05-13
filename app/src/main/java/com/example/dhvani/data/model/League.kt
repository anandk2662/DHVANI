package com.example.dhvani.data.model

import androidx.compose.ui.graphics.Color

enum class League(
    val id: Int,
    val leagueName: String,
    val xpTarget: Int,
    val trophyEmoji: String,
    val color: Color,
    val description: String
) {
    BRONZE(0, "Bronze", 0, "🥉", Color(0xFFCD7F32), "Starting your journey"),
    SILVER(1, "Silver", 500, "🥈", Color(0xFFC0C0C0), "Gaining momentum"),
    GOLD(2, "Gold", 1500, "🥇", Color(0xFFFFD700), "A rising star"),
    PLATINUM(3, "Platinum", 3000, "💎", Color(0xFFE5E4E2), "Elite learner"),
    EMERALD(4, "Emerald", 5000, "❇️", Color(0xFF50C878), "Master of signs"),
    DIAMOND(5, "Diamond", 8500, "💠", Color(0xFFB9F2FF), "Brilliant performance"),
    MASTER(6, "Master", 12500, "👑", Color(0xFF8A2BE2), "True sign expert"),
    GRANDMASTER(7, "Grandmaster", 20000, "🏆", Color(0xFFFF4500), "Top of the world"),
    CHALLENGER(8, "Challenger", 35000, "⚡", Color(0xFF00BFFF), "Defying all limits"),
    LEGEND(9, "Legend", 50000, "🌟", Color(0xFFFF00FF), "The Dhvani Legend");

    companion object {
        fun getByXp(xp: Int): League {
            return values().findLast { xp >= it.xpTarget } ?: BRONZE
        }
        
        fun getByIndex(index: Int): League {
            return values().getOrElse(index) { BRONZE }
        }
    }
}
