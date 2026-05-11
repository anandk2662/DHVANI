package com.example.dhvani.ui.screens.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.dhvani.data.model.Lesson
import com.example.dhvani.data.model.LessonStatus
import com.example.dhvani.ui.components.AnimatedCard
import com.example.dhvani.ui.components.FloatingBottomBar
import com.example.dhvani.ui.theme.*

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
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            // Ambient Background Gradients
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(PrimaryGreen.copy(alpha = 0.05f), Color.Transparent),
                            center = androidx.compose.ui.geometry.Offset(0f, 0f)
                        )
                    )
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                item {
                    ModernHomeHeader(profile?.full_name ?: "Learner")
                }

                item {
                    StatsHeroSection(
                        streak = profile?.current_streak ?: 0,
                        xp = profile?.xp_points ?: 0
                    )
                }

                item {
                    Text(
                        "Your Journey",
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                itemsIndexed(lessons) { index, lesson ->
                    JourneyNode(
                        lesson = lesson,
                        isRightSide = index % 2 != 0,
                        onNodeClick = { onLessonSelected(lesson.id) }
                    )
                    
                    if (index < lessons.size - 1) {
                        ConnectorLine(isRightToLeft = index % 2 == 0)
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                    QuickPracticeSection(onQuizClick)
                }
            }
        }
    }
}

@Composable
fun ModernHomeHeader(name: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                "Good Morning,",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray
            )
            Text(
                name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        
        Surface(
            modifier = Modifier.size(52.dp),
            shape = CircleShape,
            color = Color.White,
            shadowElevation = 4.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("👋", fontSize = 24.sp)
            }
        }
    }
}

@Composable
fun StatsHeroSection(streak: Int, xp: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        StatWidget(
            label = "Streak",
            value = "$streak Days",
            icon = Icons.Default.LocalFireDepartment,
            color = Color(0xFFFF9800),
            modifier = Modifier.weight(1f)
        )
        StatWidget(
            label = "XP Points",
            value = xp.toString(),
            icon = Icons.Default.ElectricBolt,
            color = PrimaryGreen,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun StatWidget(label: String, value: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
        }
    }
}

@Composable
fun JourneyNode(lesson: Lesson, isRightSide: Boolean, onNodeClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition()
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Reverse)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 48.dp),
        contentAlignment = if (isRightSide) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.Center) {
                if (lesson.status == LessonStatus.AVAILABLE || lesson.status == LessonStatus.IN_PROGRESS) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(PrimaryGreen.copy(alpha = glowAlpha), CircleShape)
                    )
                }
                
                Surface(
                    onClick = onNodeClick,
                    modifier = Modifier.size(80.dp),
                    shape = CircleShape,
                    color = when (lesson.status) {
                        LessonStatus.COMPLETED -> PrimaryGreen
                        LessonStatus.LOCKED -> Color.LightGray.copy(alpha = 0.5f)
                        else -> Color.White
                    },
                    shadowElevation = 8.dp,
                    border = if (lesson.status != LessonStatus.LOCKED) 
                        androidx.compose.foundation.BorderStroke(4.dp, Color.White) else null
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (lesson.status == LessonStatus.LOCKED) {
                            Text("🔒", fontSize = 24.sp)
                        } else {
                            Text(if (lesson.category == com.example.dhvani.data.model.SignCategory.ALPHABET) "A" else "1", 
                                fontSize = 32.sp, 
                                fontWeight = FontWeight.Bold,
                                color = if (lesson.status == LessonStatus.COMPLETED) Color.White else PrimaryGreen
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                lesson.title,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium,
                color = if (lesson.status == LessonStatus.LOCKED) Color.Gray else Color.Unspecified
            )
        }
    }
}

@Composable
fun ConnectorLine(isRightToLeft: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .padding(horizontal = 80.dp)
    ) {
        // This would ideally be a Canvas drawing a curved path
        // For now, using a simple vertical line or spacer to maintain the zigzag
        Spacer(
            modifier = Modifier
                .fillMaxHeight()
                .width(4.dp)
                .background(Color.LightGray.copy(alpha = 0.3f))
                .align(if (isRightToLeft) Alignment.Center else Alignment.Center)
        )
    }
}

@Composable
fun QuickPracticeSection(onQuizClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Ready for a challenge?", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleLarge)
                Text("Take a quick quiz to earn extra XP", color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onQuizClick,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Start Quiz", fontWeight = FontWeight.Bold)
                }
            }
            Text("🎯", fontSize = 64.sp, modifier = Modifier.rotate(15f))
        }
    }
}
