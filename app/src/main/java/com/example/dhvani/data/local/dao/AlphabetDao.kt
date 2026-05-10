package com.example.dhvani.data.local.dao

import androidx.room.*
import com.example.dhvani.data.local.entities.AlphabetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlphabetDao {
    @Query("SELECT * FROM alphabets")
    fun getAllAlphabets(): Flow<List<AlphabetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlphabets(alphabets: List<AlphabetEntity>)

    @Query("SELECT * FROM alphabets WHERE id = :id")
    suspend fun getAlphabetById(id: String): AlphabetEntity?
}
