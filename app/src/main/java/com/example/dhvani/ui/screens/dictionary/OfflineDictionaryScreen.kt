package com.example.dhvani.ui.screens.dictionary

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.dhvani.ui.components.DictionaryItemCard
import com.example.dhvani.ui.components.FloatingBottomBar

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

@Composable
fun OfflineDictionaryScreen(
    onHomeClick: () -> Unit,
    onPracticeClick: () -> Unit,
    onDictionaryClick: () -> Unit,
    onProfileClick: () -> Unit,
    onLeaderboardClick: () -> Unit,
    viewModel: DictionaryViewModel = hiltViewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredSigns by viewModel.filteredSigns.collectAsState()
    
    Scaffold(
        bottomBar = {
            FloatingBottomBar(
                currentScreen = "dictionary",
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
                "Sign Dictionary",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            TextField(
                value = searchQuery,
                onValueChange = { 
                    searchQuery = it
                    viewModel.onSearchQueryChange(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(16.dp)),
                placeholder = { Text("Search 2000+ signs...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                )
            )
            
            Spacer(modifier = Modifier.height(24.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 100.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredSigns) { sign ->
                    DictionaryItemCard(sign = sign)
                }
            }
        }
    }
}
