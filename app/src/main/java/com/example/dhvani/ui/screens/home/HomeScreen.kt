package com.example.dhvani.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.dhvani.ui.components.AnimatedCard
import com.example.dhvani.ui.theme.*
import com.example.dhvani.ui.components.FloatingBottomBar

@Composable
fun HomeScreen(
    onHomeClick: () -> Unit,
    onPracticeClick: () -> Unit,
    onDictionaryClick: () -> Unit,
    onProfileClick: () -> Unit,
    onLearnAlphabets: () -> Unit,
    onLearnNumbers: () -> Unit,
    onQuizClick: () -> Unit,
    onLeaderboardClick: () -> Unit,
    onLessonSelected: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val lessons by viewModel.lessons.collectAsState()
    val profile by viewModel.userProfile.collectAsState()

    Scaffold(
        bottomBar = {
            FloatingBottomBar(
                currentScreen = "home",
                onHomeClick = onHomeClick,
                onPracticeClick = onPracticeClick,
                onDictionaryClick = onDictionaryClick,
                onProfileClick = onProfileClick,
                onLeaderboardClick = onLeaderboardClick
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            contentPadding = PaddingValues(top = 24.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                HomeHeader()
            }
            
            item {
                StreakCard(profile?.current_streak ?: 0)
            }

            item {
                SectionHeader("Learning Path")
            }

            items(lessons) { lesson ->
                PathCard(
                    title = lesson.title,
                    subtitle = "${lesson.signs.size} signs",
                    progress = if (lesson.status == com.example.dhvani.data.model.LessonStatus.COMPLETED) 1f else 0f,
                    gradient = if (lesson.category == com.example.dhvani.data.model.SignCategory.ALPHABET) BeginnerGradient else IntermediateGradient,
                    icon = if (lesson.category == com.example.dhvani.data.model.SignCategory.ALPHABET) "🔤" else "🔢",
                    isLocked = lesson.isLocked,
                    onClick = { onLessonSelected(lesson.id) }
                )
            }
            
            item {
                SectionHeader("Test Your Skills")
            }

            item {
                PathCard(
                    title = "Take a Quiz",
                    subtitle = "Test what you've learned",
                    progress = 0f,
                    gradient = AdvancedGradient,
                    icon = "🎯",
                    onClick = onQuizClick
                )
            }
            
            item {
                DailyChallengeCard()
            }
        }
    }
}

@Composable
fun HomeHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                "Hello, Learner!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                "Ready to learn sign language today?",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }
        
        Surface(
            modifier = Modifier.size(56.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("👤", fontSize = 24.sp)
            }
        }
    }
}

@Composable
fun StreakCard(streak: Int) {
    AnimatedCard(onClick = {}) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(Color(0xFFFF9800), Color(0xFFFF5722))))
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.2f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("🔥", fontSize = 24.sp)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    if (streak > 0) "$streak Day Streak!" else "Start Your Journey!",
                    color = Color.White, 
                    fontWeight = FontWeight.Bold, 
                    fontSize = 18.sp
                )
                Text(
                    if (streak > 0) "Keep it up for tomorrow!" else "Keep learning to build a streak.",
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun PathCard(
    title: String,
    subtitle: String,
    progress: Float,
    gradient: List<Color>,
    icon: String,
    isLocked: Boolean = false,
    onClick: () -> Unit = {}
) {
    AnimatedCard(onClick = if (isLocked) ({}) else onClick) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (isLocked) Color.LightGray.copy(alpha = 0.3f) else Color.Transparent)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = if (isLocked) Color.Gray.copy(alpha = 0.2f) else gradient[0].copy(alpha = 0.2f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(if (isLocked) "🔒" else icon, fontSize = 24.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                    }
                }
                
                if (!isLocked && progress > 0f) {
                    Spacer(modifier = Modifier.height(16.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                        color = gradient[0],
                        trackColor = gradient[0].copy(alpha = 0.1f)
                    )
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun DailyChallengeCard() {
    AnimatedCard(onClick = {}) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(AdvancedGradient))
                .padding(20.dp)
        ) {
            Text("Daily Challenge", color = Color.White, fontWeight = FontWeight.Bold)
            Text("Complete a perfect practice session", color = Color.White.copy(alpha = 0.8f))
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {},
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Start Now", color = AdvancedGradient[1], fontWeight = FontWeight.Bold)
            }
        }
    }
}
