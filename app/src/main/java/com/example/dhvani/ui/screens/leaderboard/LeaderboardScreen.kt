package com.example.dhvani.ui.screens.leaderboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.dhvani.data.repository.UserProfile
import com.example.dhvani.ui.components.FloatingBottomBar
import com.example.dhvani.ui.theme.PrimaryGreen

@Composable
fun LeaderboardScreen(
    onHomeClick: () -> Unit,
    onPracticeClick: () -> Unit,
    onDictionaryClick: () -> Unit,
    onProfileClick: () -> Unit,
    onLeaderboardClick: () -> Unit,
    viewModel: LeaderboardViewModel = hiltViewModel()
) {
    val users by viewModel.users.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        bottomBar = {
            FloatingBottomBar(
                currentScreen = "leaderboard",
                onHomeClick = onHomeClick,
                onPracticeClick = onPracticeClick,
                onDictionaryClick = onDictionaryClick,
                onProfileClick = onProfileClick,
                onLeaderboardClick = onLeaderboardClick
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "Leaderboard",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "See how you rank against others",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            
            Spacer(modifier = Modifier.height(24.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    itemsIndexed(users) { index, user ->
                        LeaderboardItem(rank = index + 1, user = user)
                    }
                }
            }
        }
    }
}

@Composable
fun LeaderboardItem(rank: Int, user: UserProfile) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = rank.toString(),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = if (rank <= 3) PrimaryGreen else Color.Gray,
                modifier = Modifier.width(32.dp)
            )
            
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(user.username?.take(1)?.uppercase() ?: "?")
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(user.username ?: "Anonymous", fontWeight = FontWeight.Bold)
                Text("${user.xp_points} XP", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            if (user.current_streak > 0) {
                Text("🔥 ${user.current_streak}", fontWeight = FontWeight.Bold, color = Color(0xFFFF9800))
            }
        }
    }
}
