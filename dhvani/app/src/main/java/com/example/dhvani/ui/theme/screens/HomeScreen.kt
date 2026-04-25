package com.example.dhvani.ui.theme.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.dhvani.viewmodel.AuthViewModel

@Composable
fun HomeScreen(navController: NavController, viewModel: AuthViewModel = viewModel()){
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Welcome to Home Screen!")
        
        Spacer(modifier = Modifier.height(20.dp))
        
        Button(onClick = {
            viewModel.logout()
            navController.navigate("welcome") {
                popUpTo(0)
            }
        }) {
            Text("Logout")
        }
    }
}