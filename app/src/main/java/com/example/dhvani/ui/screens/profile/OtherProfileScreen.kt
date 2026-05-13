package com.example.dhvani.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import com.example.dhvani.ui.theme.AccentPurple

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtherProfileScreen(
    userId: String,
    onBackClick: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val otherProfile by viewModel.otherProfile.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    LaunchedEffect(userId) {
        viewModel.fetchOtherProfile(userId)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(otherProfile?.username ?: "Profile") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).background(MaterialTheme.colorScheme.background)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item {
                        ProfileHeader(otherProfile)
                    }
                    
                    item {
                        Row(
                            modifier = Modifier.padding(24.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            StatCard("XP", otherProfile?.xp_points?.toString() ?: "0", Icons.Default.FlashOn, AccentPurple, Modifier.weight(1f))
                            StatCard("Streak", otherProfile?.current_streak?.toString() ?: "0", Icons.Default.LocalFireDepartment, Color(0xFFFF9800), Modifier.weight(1f))
                        }
                    }

                    item {
                        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                            otherProfile?.region?.let { region ->
                                Text(
                                    text = "Region: $region",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }

                    item {
                        Column(modifier = Modifier.padding(24.dp)) {
                            val isFriend = viewModel.profile.collectAsState().value?.friend_ids?.contains(userId) == true
                            
                            Button(
                                onClick = { if (!isFriend) viewModel.addFriend(userId) },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
                                enabled = !isFriend,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isFriend) Color.Gray else MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(if (isFriend) Icons.Default.Check else Icons.Default.PersonAdd, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (isFriend) "FRIENDS" else "ADD FRIEND")
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            OutlinedButton(
                                onClick = { viewModel.syncSharedStreak(userId) },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp)
                            ) {
                                Icon(Icons.Default.Groups, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("MAINTAIN STREAK TOGETHER")
                            }
                        }
                    }
                    
                    item {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Text("Shared Streaks", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            val sharedStreakCount = viewModel.profile.collectAsState().value?.shared_streaks?.get(userId) ?: 0
                            
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (sharedStreakCount > 0) Color(0xFFFFF3E0).copy(alpha = 0.8f) else Color.LightGray.copy(alpha = 0.1f)
                                )
                            ) {
                                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(if (sharedStreakCount > 0) "🔥" else "🌑", fontSize = 24.sp)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(if (sharedStreakCount > 0) "$sharedStreakCount Day Duo Streak" else "No shared streak yet", fontWeight = FontWeight.Bold)
                                        Text(if (sharedStreakCount > 0) "Keep practicing to grow it!" else "Start practicing together!", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
