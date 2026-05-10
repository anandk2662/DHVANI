package com.example.dhvani.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.dhvani.data.local.dao.AlphabetDao
import com.example.dhvani.data.local.entities.AlphabetEntity

@Database(entities = [AlphabetEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun alphabetDao(): AlphabetDao
}
