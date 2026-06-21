package com.example.stego.db

import kotlinx.coroutines.flow.Flow

class StegoRepository(private val stegoHistoryDao: StegoHistoryDao) {
    val allHistory: Flow<List<StegoHistory>> = stegoHistoryDao.getAllHistory()

    suspend fun insert(history: StegoHistory) {
        stegoHistoryDao.insertHistory(history)
    }

    suspend fun deleteById(id: Int) {
        stegoHistoryDao.deleteHistoryById(id)
    }

    suspend fun clearAll() {
        stegoHistoryDao.clearAllHistory()
    }
}
