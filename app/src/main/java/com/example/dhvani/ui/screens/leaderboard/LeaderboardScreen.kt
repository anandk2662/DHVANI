package com.example.dhvani.ui.screens.leaderboard

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.dhvani.data.model.League
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
    onUserClick: (String) -> Unit,
    viewModel: LeaderboardViewModel = hiltViewModel()
) {
    val users by viewModel.users.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    var selectedLeagueIndex by remember { mutableIntStateOf(0) }

    // Automatically select current user's league on first load
    LaunchedEffect(currentUser) {
        currentUser?.let {
            selectedLeagueIndex = League.getByXp(it.xp_points).id
        }
    }

    // Fetch data when league changes
    LaunchedEffect(selectedLeagueIndex) {
        viewModel.fetchLeaderboard(selectedLeagueIndex)
    }

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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Ambient Background Gradient
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
            )

            Column(modifier = Modifier.fillMaxSize()) {
                // Premium Header
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    shadowElevation = 4.dp,
                    shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(PrimaryGreen, PrimaryGreen.copy(alpha = 0.7f))
                                )
                            )
                            .padding(horizontal = 24.dp, vertical = 32.dp)
                    ) {
                        Text(
                            "Leagues",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            "Rise through the ranks to become a Legend",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // League Selector Row
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(end = 24.dp)
                        ) {
                            items(League.entries) { league ->
                                LeagueTab(
                                    league = league,
                                    isSelected = selectedLeagueIndex == league.id,
                                    onClick = { selectedLeagueIndex = league.id }
                                )
                            }
                        }
                    }
                }

                val filteredUsers = users.filter { 
                    League.getByXp(it.xp_points).id == selectedLeagueIndex
                }.sortedByDescending { it.xp_points }

                if (isLoading) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryGreen)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 24.dp)
                    ) {
                        if (filteredUsers.isEmpty()) {
                            item {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(top = 60.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("🌑", fontSize = 48.sp)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        "No competitors here yet!",
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        } else {
                            itemsIndexed(filteredUsers) { index, user ->
                                val isMe = user.id == currentUser?.id
                                LeaderboardItem(
                                    rank = index + 1,
                                    user = user,
                                    isCurrentUser = isMe,
                                    onClick = { onUserClick(user.id) }
                                )
                            }
                        }
                        
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LeagueTab(league: League, isSelected: Boolean, onClick: () -> Unit) {
    val backgroundColor = if (isSelected) Color.White else Color.White.copy(alpha = 0.15f)
    val contentColor = if (isSelected) league.color else Color.White

    Surface(
        onClick = onClick,
        modifier = Modifier
            .height(40.dp)
            .clip(RoundedCornerShape(20.dp)),
        color = backgroundColor,
        tonalElevation = if (isSelected) 4.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(league.trophyEmoji, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                league.leagueName,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = contentColor
            )
        }
    }
}

@Composable
fun LeaderboardItem(rank: Int, user: UserProfile, isCurrentUser: Boolean, onClick: () -> Unit) {
    val league = League.getByXp(user.xp_points)
    
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = if (isCurrentUser) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface,
        tonalElevation = if (isCurrentUser) 4.dp else 1.dp,
        shadowElevation = if (isCurrentUser) 4.dp else 2.dp,
        border = when {
            isCurrentUser -> androidx.compose.foundation.BorderStroke(2.dp, PrimaryGreen)
            rank <= 3 -> androidx.compose.foundation.BorderStroke(1.dp, league.color.copy(alpha = 0.3f))
            else -> null
        }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank Badge with Glow for Top 3
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        when (rank) {
                            1 -> Color(0xFFFFD700).copy(alpha = 0.2f)
                            2 -> Color(0xFFC0C0C0).copy(alpha = 0.2f)
                            3 -> Color(0xFFCD7F32).copy(alpha = 0.2f)
                            else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                        },
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = rank.toString(),
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = when (rank) {
                        1 -> Color(0xFFFFD700)
                        2 -> Color(0xFFC0C0C0)
                        3 -> Color(0xFFCD7F32)
                        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    }
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Avatar with animated-like border
            Box {
                Surface(
                    modifier = Modifier.size(52.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    border = androidx.compose.foundation.BorderStroke(2.dp, league.color)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            user.username?.take(1)?.uppercase() ?: "?",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                // Floating league badge
                Surface(
                    modifier = Modifier
                        .size(20.dp)
                        .align(Alignment.BottomEnd)
                        .offset(x = 2.dp, y = 2.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 2.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(league.trophyEmoji, fontSize = 10.sp)
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    user.username ?: "Anonymous",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "${user.xp_points} XP",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            
            // Streak Badge
            if (user.current_streak > 0) {
                Surface(
                    color = Color(0xFFFF9800).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            Icons.Default.Whatshot,
                            contentDescription = null,
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            user.current_streak.toString(),
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFFF9800),
                            fontSize = 12.sp
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            IconButton(
                onClick = { /* Add Friend Logic */ },
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = PrimaryGreen
                )
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Add Friend", modifier = Modifier.size(22.dp))
            }
        }
    }
}
