package com.example.dhvani.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dhvani.ui.components.AnimatedCard
import com.example.dhvani.ui.theme.*

data class Level(
    val title: String, 
    val description: String, 
    val gradient: List<Color>,
    val icon: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LevelSelectionScreen(
    onLevelSelected: (Level) -> Unit,
    onBackClick: () -> Unit
) {
    val levels = listOf(
        Level("Beginner", "Start with the basics: Alphabets and simple greetings.", BeginnerGradient, "🌱"),
        Level("Intermediate", "Learn common phrases and conversational signs.", IntermediateGradient, "🤝"),
        Level("Advanced", "Master complex sentences and specialized vocabulary.", AdvancedGradient, "🏆")
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Choose your level", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(vertical = 24.dp)
        ) {
            item {
                Text(
                    "We'll recommend lessons based on your experience.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            items(levels) { level ->
                LevelCard(level = level, onClick = { onLevelSelected(level) })
            }
        }
    }
}

@Composable
fun LevelCard(level: Level, onClick: () -> Unit) {
    AnimatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .background(Brush.linearGradient(level.gradient))
                .padding(24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(64.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.2f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(level.icon, fontSize = 32.sp)
                    }
                }
                
                Spacer(modifier = Modifier.width(20.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = level.title, 
                        fontWeight = FontWeight.Bold, 
                        fontSize = 20.sp,
                        color = Color.White
                    )
                    Text(
                        text = level.description, 
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
