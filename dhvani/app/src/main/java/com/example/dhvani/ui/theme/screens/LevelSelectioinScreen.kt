package com.example.dhvani.ui.theme.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun LevelSelectionScreen(navController: NavController){

    val levels=listOf<String>("Beginner","Intermediate","Advanced")
    var selected by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        levels.forEach {
            level->
            val isSelected=level==selected
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .background(
                        if (isSelected) Color(0xFF2196F3) else Color.LightGray,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable { selected = level }
                    .padding(16.dp)
                    ) {
                Text(
                    text = level,
                    color = if (isSelected) Color.White else Color.Black,
                )
            }
        }
        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                selected?.let {
                    navController.navigate("quiz/$it")
                }
            },
            enabled = selected!=null,
            modifier = Modifier.fillMaxWidth()
        ){
            Text("Start Learning")
        }
    }
}