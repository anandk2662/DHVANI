package com.example.dhvani.ui.screens.dictionary

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dhvani.ui.components.AnimatedCard
import com.example.dhvani.ui.components.FloatingBottomBar
import com.example.dhvani.ui.components.PremiumTextField

data class SignItem(val label: String, val icon: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DictionaryScreen(
    onHomeClick: () -> Unit,
    onPracticeClick: () -> Unit,
    onDictionaryClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val signs = listOf(
        SignItem("Apple", "🍎"), SignItem("Ball", "⚽"), SignItem("Cat", "🐱"),
        SignItem("Dog", "🐶"), SignItem("Eat", "🍽️"), SignItem("Father", "👨"),
        SignItem("Go", "🚶"), SignItem("Home", "🏠"), SignItem("Ice", "🧊")
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Dictionary", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            FloatingBottomBar(
                currentScreen = "dictionary",
                onHomeClick = onHomeClick,
                onPracticeClick = onPracticeClick,
                onDictionaryClick = onDictionaryClick,
                onProfileClick = onProfileClick
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            PremiumTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = "Search for a word...",
                leadingIcon = Icons.Default.Search
            )

            Spacer(modifier = Modifier.height(24.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                items(signs) { sign ->
                    SignCard(sign)
                }
            }
        }
    }
}

@Composable
fun SignCard(sign: SignItem) {
    AnimatedCard(onClick = {}) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(sign.icon, fontSize = 40.sp)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(sign.label, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}
