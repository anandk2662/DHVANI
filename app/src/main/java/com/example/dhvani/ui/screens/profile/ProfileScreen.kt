package com.example.dhvani.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.dhvani.data.repository.UserProfile
import com.example.dhvani.ui.components.AnimatedCard
import com.example.dhvani.ui.components.FloatingBottomBar
import com.example.dhvani.ui.theme.*

@Composable
fun ProfileScreen(
    onHomeClick: () -> Unit,
    onPracticeClick: () -> Unit,
    onDictionaryClick: () -> Unit,
    onProfileClick: () -> Unit,
    onLeaderboardClick: () -> Unit,
    onLogoutSuccess: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val profile by viewModel.profile.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val aiModelUrl by viewModel.aiModelUrl.collectAsState()
    var showEditDialog by remember { mutableStateOf(false) }
    var showAiUrlDialog by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            FloatingBottomBar(
                currentScreen = "profile",
                onHomeClick = onHomeClick,
                onPracticeClick = onPracticeClick,
                onDictionaryClick = onDictionaryClick,
                onProfileClick = onProfileClick,
                onLeaderboardClick = onLeaderboardClick
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).background(MaterialTheme.colorScheme.background)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    item {
                        ProfileHeader(profile)
                    }
                    
                    item {
                        Row(
                            modifier = Modifier.padding(24.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            StatCard("XP", profile?.xp_points?.toString() ?: "0", Icons.Default.FlashOn, com.example.dhvani.ui.theme.AccentPurple, Modifier.weight(1f))
                            StatCard("Streak", profile?.current_streak?.toString() ?: "0", Icons.Default.LocalFireDepartment, Color(0xFFFF9800), Modifier.weight(1f))
                        }
                    }

                    item {
                        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                            Text("Settings", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(16.dp))
                            SettingsItem(Icons.Default.Person, "Edit Profile") {
                                showEditDialog = true
                            }
                            SettingsItem(Icons.Default.Notifications, "Notifications") {}
                            SettingsItem(Icons.Default.Security, "Privacy & Security") {}
                            SettingsItem(Icons.Default.Settings, "AI Preference") {
                                showAiUrlDialog = true
                            }
                            SettingsItem(Icons.AutoMirrored.Filled.Help, "Help Center") {}
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            Button(
                                onClick = { 
                                    viewModel.logout {
                                        onLogoutSuccess()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = ErrorRed.copy(alpha = 0.1f)),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text("Log Out", color = ErrorRed, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEditDialog && profile != null) {
        EditProfileDialog(
            profile = profile!!,
            onDismiss = { showEditDialog = false },
            onSave = { username, fullName, region ->
                viewModel.updateProfile(username, fullName, region)
                showEditDialog = false
            }
        )
    }

    if (showAiUrlDialog) {
        AiUrlDialog(
            currentUrl = aiModelUrl,
            onDismiss = { showAiUrlDialog = false },
            onSave = { url ->
                viewModel.updateAiUrl(url)
                showAiUrlDialog = false
            }
        )
    }
}

@Composable
fun AiUrlDialog(
    currentUrl: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var url by remember { mutableStateOf(currentUrl) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("AI Model Configuration") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Enter the endpoint URL for hand sign recognition API.", style = MaterialTheme.typography.bodySmall)
                TextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("API URL") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("https://...") }
                )
                Text("The app will POST to this URL with /sign appended if needed.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(url) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ProfileHeader(profile: UserProfile?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f)
                .background(Brush.verticalGradient(listOf(PrimaryGreen, SecondaryBlue)))
        )
        
        Column(
            modifier = Modifier.align(Alignment.BottomCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier
                    .size(120.dp)
                    .border(4.dp, Color.White, CircleShape),
                shape = CircleShape,
                color = LightBackground,
                shadowElevation = 8.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("👤", fontSize = 64.sp)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(profile?.full_name ?: "Guest User", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("@${profile?.username ?: "guest"} • ${profile?.region ?: "Unknown Region"}", color = Color.Gray)
        }
    }
}

@Composable
fun StatCard(label: String, value: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    AnimatedCard(onClick = {}, modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
            Text(label, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Text(label, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.LightGray)
        }
    }
}

@Composable
fun EditProfileDialog(
    profile: UserProfile,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var username by remember { mutableStateOf(profile.username ?: "") }
    var fullName by remember { mutableStateOf(profile.full_name ?: "") }
    var region by remember { mutableStateOf(profile.region ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Profile") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(value = fullName, onValueChange = { fullName = it }, label = { Text("Full Name") })
                TextField(value = username, onValueChange = { username = it }, label = { Text("Username") })
                TextField(value = region, onValueChange = { region = it }, label = { Text("Region") })
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(username, fullName, region) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
