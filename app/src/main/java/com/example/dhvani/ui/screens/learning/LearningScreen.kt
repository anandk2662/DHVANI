package com.example.dhvani.ui.screens.learning

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.dhvani.data.model.SignCategory
import com.example.dhvani.ui.components.GradientButton
import com.example.dhvani.ui.components.SignCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearningScreen(
    category: SignCategory,
    onBack: () -> Unit,
    viewModel: LearningViewModel = hiltViewModel()
) {
    val signs by viewModel.signs.collectAsState()
    val currentIndex by viewModel.currentIndex.collectAsState()

    LaunchedEffect(category) {
        viewModel.loadCategory(category)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (category == SignCategory.ALPHABET) "Learn Alphabets" else "Learn Numbers") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (signs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val currentSign = signs[currentIndex]
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LinearProgressIndicator(
                    progress = { (currentIndex + 1).toFloat() / signs.size },
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    "Step ${currentIndex + 1} of ${signs.size}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(32.dp))

                SignCard(
                    sign = currentSign,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.previous() },
                        modifier = Modifier.weight(1f).height(56.dp),
                        enabled = currentIndex > 0,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("PREVIOUS")
                    }

                    GradientButton(
                        text = if (currentIndex < signs.size - 1) "NEXT" else "FINISH",
                        onClick = {
                            if (currentIndex < signs.size - 1) {
                                viewModel.next()
                            } else {
                                onBack()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
