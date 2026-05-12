package com.example.dhvani.ui.screens.dictionary

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dhvani.ui.components.FloatingBottomBar
import com.example.dhvani.ui.components.PremiumTextField
import com.example.dhvani.data.model.SignItem
import com.example.dhvani.ui.components.DictionaryItemCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DictionaryScreen(
    onHomeClick: () -> Unit,
    onPracticeClick: () -> Unit,
    onDictionaryClick: () -> Unit,
    onProfileClick: () -> Unit,
    viewModel: DictionaryViewModel = hiltViewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    val allSigns by viewModel.allSigns.collectAsState()
    
    val filteredSigns = if (searchQuery.isEmpty()) {
        allSigns
    } else {
        allSigns.filter { it.label.contains(searchQuery, ignoreCase = true) }
    }

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
                items(filteredSigns) { sign ->
                    DictionaryItemCard(sign = sign)
                }
            }
        }
    }
}
