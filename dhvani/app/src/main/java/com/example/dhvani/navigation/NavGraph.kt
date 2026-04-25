package com.example.dhvani.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.dhvani.ui.theme.screens.HomeScreen
import com.example.dhvani.ui.theme.screens.LevelSelectionScreen
import com.example.dhvani.ui.theme.screens.LoginScreen
import com.example.dhvani.ui.theme.screens.QuizScreen
import com.example.dhvani.ui.theme.screens.ResultScreen
import com.example.dhvani.ui.theme.screens.StarterChoiceScreen
import com.example.dhvani.ui.theme.screens.WelcomeScreen
import com.example.dhvani.viewmodel.AuthViewModel

@Composable
fun NavGraph(){
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    
    // Check if user is already logged in to determine start destination
    val startDestination = if (authViewModel.isLoggedIn()) "home" else "welcome"

    NavHost(
        navController = navController,
        startDestination = startDestination
    ){
        composable("welcome"){
            WelcomeScreen(navController = navController)
        }
        composable("login"){
            LoginScreen(viewModel = authViewModel, navController = navController)
        }
        composable("home"){
            HomeScreen(navController = navController, viewModel = authViewModel)
        }
        composable("choice"){
            StarterChoiceScreen(navController=navController)
        }
        composable("level"){
            LevelSelectionScreen(navController=navController)
        }
        composable("quiz/{quizId}"){ backStackEntry ->
            val quizId = backStackEntry.arguments?.getString("quizId")
            QuizScreen(navController = navController, quizId = quizId)

        }
        composable(
            "result/{total}/{score}",
            arguments = listOf(
                navArgument("total") { type = NavType.IntType },
                navArgument("score") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val total = backStackEntry.arguments?.getInt("total") ?: 0
            val score = backStackEntry.arguments?.getInt("score") ?: 0
            ResultScreen(score = score, total = total, navController = navController)
        }
    }
}