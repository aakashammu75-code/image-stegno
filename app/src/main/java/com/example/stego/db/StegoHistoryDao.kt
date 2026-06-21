package com.example.stego.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StegoHistoryDao {
    @Query("SELECT * FROM stego_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<StegoHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: StegoHistory)

    @Query("DELETE FROM stego_history WHERE id = :id")
    suspend fun deleteHistoryById(id: Int)

    @Query("DELETE FROM stego_history")
    suspend fun clearAllHistory()
}
