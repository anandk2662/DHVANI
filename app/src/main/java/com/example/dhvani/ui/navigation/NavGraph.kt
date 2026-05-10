package com.example.dhvani.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.dhvani.data.model.SignCategory
import com.example.dhvani.ui.screens.auth.LoginScreen
import com.example.dhvani.ui.screens.auth.OtpVerificationScreen
import com.example.dhvani.ui.screens.auth.SignupScreen
import com.example.dhvani.ui.screens.dictionary.OfflineDictionaryScreen
import com.example.dhvani.ui.screens.home.HomeScreen
import com.example.dhvani.ui.screens.learning.LearningScreen
import com.example.dhvani.ui.screens.onboarding.LevelSelectionScreen
import com.example.dhvani.ui.screens.onboarding.SplashScreen
import com.example.dhvani.ui.screens.onboarding.WelcomeScreen
import com.example.dhvani.ui.screens.practice.PracticeScreen
import com.example.dhvani.ui.screens.profile.ProfileScreen
import com.example.dhvani.ui.screens.quiz.OfflineQuizScreen
import com.example.dhvani.ui.screens.lesson.LessonScreen
import com.example.dhvani.ui.screens.leaderboard.LeaderboardScreen
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth

@Composable
fun AppNavGraph(
    navController: NavHostController,
    supabaseClient: SupabaseClient
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToWelcome = {
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Welcome.route) {
            WelcomeScreen(
                onLoginClick = { navController.navigate(Screen.Login.route) },
                onGetStartedClick = { navController.navigate(Screen.LevelSelection.route) }
            )
        }
        composable(Screen.LevelSelection.route) {
            LevelSelectionScreen(
                onLevelSelected = { level ->
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { 
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                },
                onSignupClick = { navController.navigate(Screen.Signup.route) },
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(Screen.Signup.route) {
            SignupScreen(
                onSignupStarted = { email ->
                    navController.navigate(Screen.OtpVerification.createRoute(email))
                },
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.OtpVerification.route,
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            OtpVerificationScreen(
                email = email,
                onVerificationSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(Screen.Quiz.route) {
            OfflineQuizScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Leaderboard.route) {
            LeaderboardScreen(
                onHomeClick = { navController.navigate(Screen.Home.route) },
                onPracticeClick = { navController.navigate(Screen.Practice.route) },
                onDictionaryClick = { navController.navigate(Screen.Dictionary.route) },
                onProfileClick = { 
                    if (supabaseClient.auth.currentUserOrNull() != null) {
                        navController.navigate(Screen.Profile.route)
                    } else {
                        navController.navigate(Screen.Login.route)
                    }
                },
                onLeaderboardClick = { }
            )
        }
        composable(Screen.Home.route) {
            HomeScreen(
                onHomeClick = { },
                onPracticeClick = { navController.navigate(Screen.Practice.route) },
                onDictionaryClick = { navController.navigate(Screen.Dictionary.route) },
                onProfileClick = { 
                    if (supabaseClient.auth.currentUserOrNull() != null) {
                        navController.navigate(Screen.Profile.route)
                    } else {
                        navController.navigate(Screen.Login.route)
                    }
                },
                onLeaderboardClick = { navController.navigate(Screen.Leaderboard.route) },
                onLearnAlphabets = {
                    navController.navigate(Screen.Learning.createRoute(SignCategory.ALPHABET.name))
                },
                onLearnNumbers = {
                    navController.navigate(Screen.Learning.createRoute(SignCategory.NUMBER.name))
                },
                onQuizClick = {
                    navController.navigate(Screen.Quiz.route)
                },
                onLessonSelected = { lessonId ->
                    navController.navigate(Screen.Lesson.createRoute(lessonId))
                }
            )
        }
        composable(
            route = Screen.Lesson.route,
            arguments = listOf(navArgument("lessonId") { type = NavType.StringType })
        ) { backStackEntry ->
            val lessonId = backStackEntry.arguments?.getString("lessonId") ?: ""
            LessonScreen(
                lessonId = lessonId,
                onBack = { 
                    navController.popBackStack(Screen.Home.route, inclusive = false)
                }
            )
        }
        composable(Screen.Practice.route) {
            PracticeScreen(
                onHomeClick = { navController.navigate(Screen.Home.route) },
                onPracticeClick = { },
                onDictionaryClick = { navController.navigate(Screen.Dictionary.route) },
                onProfileClick = { 
                    if (supabaseClient.auth.currentUserOrNull() != null) {
                        navController.navigate(Screen.Profile.route)
                    } else {
                        navController.navigate(Screen.Login.route)
                    }
                },
                onLeaderboardClick = { navController.navigate(Screen.Leaderboard.route) },
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(Screen.Dictionary.route) {
            OfflineDictionaryScreen(
                onHomeClick = { navController.navigate(Screen.Home.route) },
                onPracticeClick = { navController.navigate(Screen.Practice.route) },
                onDictionaryClick = { },
                onProfileClick = { 
                    if (supabaseClient.auth.currentUserOrNull() != null) {
                        navController.navigate(Screen.Profile.route)
                    } else {
                        navController.navigate(Screen.Login.route)
                    }
                },
                onLeaderboardClick = { navController.navigate(Screen.Leaderboard.route) }
            )
        }
        composable(Screen.Profile.route) {
            ProfileScreen(
                onHomeClick = { navController.navigate(Screen.Home.route) },
                onPracticeClick = { navController.navigate(Screen.Practice.route) },
                onDictionaryClick = { navController.navigate(Screen.Dictionary.route) },
                onProfileClick = { },
                onLeaderboardClick = { navController.navigate(Screen.Leaderboard.route) },
                onLogoutSuccess = {
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable(
            route = Screen.Learning.route,
            arguments = listOf(navArgument("category") { type = NavType.StringType })
        ) { backStackEntry ->
            val categoryName = backStackEntry.arguments?.getString("category")
            val category = SignCategory.valueOf(categoryName ?: SignCategory.ALPHABET.name)
            LearningScreen(
                category = category,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
