package com.example.dhvani.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alphabets")
data class AlphabetEntity(
    @PrimaryKey val id: String,
    val char: String,
    val imageUrl: String,
    val description: String,
    val category: String
)
