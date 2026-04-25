package com.example.dhvani.ui.theme.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dhvani.ui.theme.components.ChoiceCard

@Composable
fun StarterChoiceScreen(navController: NavController){

    var selected by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5FAFF))
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "How would you like to start?",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )


    Spacer(modifier = Modifier.height(20.dp))

    ChoiceCard(
        title = "Start from Scratch",
        isSelected = selected =="Scratch",
        onClick = {
            selected ="scratch"
            navController.navigate("home")
        }
    )

    Spacer(modifier = Modifier.height(20.dp))

    ChoiceCard(
        title = "Find My Level",
        isSelected = selected =="level",
        onClick = {
            selected ="level"
            navController.navigate("level")
        }
    )
    }
}