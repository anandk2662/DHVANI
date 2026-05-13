package com.example.dhvani.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VideoCameraFront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dhvani.ui.theme.TextOnPrimary

@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    gradient: List<Color> = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary),
    textColor: Color = TextOnPrimary,
    enabled: Boolean = true
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isPressed) 0.95f else 1f, label = "ButtonScale")

    Button(
        onClick = onClick,
        modifier = modifier
            .scale(scale)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = { if (enabled) onClick() }
                )
            },
        contentPadding = PaddingValues(),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(16.dp),
        enabled = enabled
    ) {
        Box(
            modifier = Modifier
                .background(Brush.linearGradient(gradient))
                .padding(vertical = 16.dp, horizontal = 24.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = if (enabled) textColor else textColor.copy(alpha = 0.5f),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

/*@Composable
fun AnimatedCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    content: @Composable ColumnScope.() -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isPressed) 0.98f else 1f, label = "CardScale")
    val elevation by animateFloatAsState(if (isPressed) 2f else 6f, label = "CardElevation")

    Card(
        modifier = modifier
            .scale(scale)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = { onClick() }
                )
            },
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        content()
    }
}
*/
@Composable
fun PremiumTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation = androidx.compose.ui.text.input.VisualTransformation.None,
    keyboardOptions: androidx.compose.foundation.text.KeyboardOptions = androidx.compose.foundation.text.KeyboardOptions.Default
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        leadingIcon = leadingIcon?.let { { Icon(it, contentDescription = null) } },
        shape = RoundedCornerShape(16.dp),
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    )
}

@Composable
fun FloatingBottomBar(
    currentScreen: String,
    onHomeClick: () -> Unit,
    onPracticeClick: () -> Unit,
    onDictionaryClick: () -> Unit,
    onProfileClick: () -> Unit,
    onLeaderboardClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 12.dp
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomNavItem(Icons.Default.Home, "Home", currentScreen == "home") { onHomeClick() }
                BottomNavItem(Icons.Default.VideoCameraFront, "Practice", currentScreen == "practice") { onPracticeClick() }
                BottomNavItem(Icons.Default.Leaderboard, "Leaderboard", currentScreen == "leaderboard") { onLeaderboardClick() }
                BottomNavItem(Icons.Default.Book, "Dictionary", currentScreen == "dictionary") { onDictionaryClick() }
                BottomNavItem(Icons.Default.Person, "Profile", currentScreen == "profile") { onProfileClick() }
            }
        }
    }
}

@Composable
fun BottomNavItem(icon: ImageVector, label: String, isSelected: Boolean, onClick: () -> Unit) {
    val scale by animateFloatAsState(if (isSelected) 1.2f else 1f, label = "NavScale")
    val color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray
    
    IconButton(onClick = onClick) {
        Icon(
            icon, 
            contentDescription = label,
            modifier = Modifier.scale(scale),
            tint = color
        )
    }
}
