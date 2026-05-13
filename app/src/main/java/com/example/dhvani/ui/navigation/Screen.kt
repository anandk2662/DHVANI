package com.example.dhvani.ui.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Welcome : Screen("welcome")
    data object LevelSelection : Screen("level_selection")
    data object Login : Screen("login")
    data object Signup : Screen("signup")
    data object OtpVerification : Screen("otp_verification/{email}") {
        fun createRoute(email: String) = "otp_verification/$email"
    }
    data object Home : Screen("home")
    data object Practice : Screen("practice")
    data object Dictionary : Screen("dictionary")
    data object Profile : Screen("profile")
    data object Quiz : Screen("quiz")
    data object Leaderboard : Screen("leaderboard")
    data object OtherProfile : Screen("other_profile/{userId}") {
        fun createRoute(userId: String) = "other_profile/$userId"
    }
    data object Lesson : Screen("lesson/{lessonId}") {
        fun createRoute(lessonId: String) = "lesson/$lessonId"
    }
    data object Learning : Screen("learning/{category}") {
        fun createRoute(category: String) = "learning/$category"
    }
}
