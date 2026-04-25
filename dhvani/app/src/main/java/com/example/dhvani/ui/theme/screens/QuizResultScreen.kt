package com.example.dhvani.ui.theme.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

@Composable
fun ResultScreen(score: Int, total: Int, navController: NavController) {

    val percentage = (score * 100) / total

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text("Score: $score / $total")
        Text("Percentage: $percentage%")

        Button(onClick = {
            navController.navigate("home")
        }) {
            Text("Continue")
        }
    }
}