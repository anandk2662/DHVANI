package com.example.dhvani

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.dhvani.ui.navigation.AppNavGraph
import com.example.dhvani.ui.theme.DhvaniTheme
import dagger.hilt.android.AndroidEntryPoint
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.handleDeeplinks
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var supabaseClient: SupabaseClient

    @Inject
    lateinit var gamificationEngine: com.example.dhvani.gamification.GamificationEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        supabaseClient.handleDeeplinks(intent)
        
        // Check for broken streak on app start
        lifecycleScope.launch {
            gamificationEngine.checkAndResetBrokenStreak()
        }
        
        enableEdgeToEdge()
        setContent {
            DhvaniTheme {
                val navController = rememberNavController()
                
                AppNavGraph(
                    navController = navController,
                    supabaseClient = supabaseClient
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        supabaseClient.handleDeeplinks(intent)
    }
}
