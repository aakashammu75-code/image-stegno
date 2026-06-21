package com.example.stego.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stego_history")
data class StegoHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val actionType: String, // "HIDE" or "EXTRACT"
    val timestamp: Long = System.currentTimeMillis(),
    val imageName: String,
    val payloadSize: Int, // Number of bytes hidden or extracted
    val isEncrypted: Boolean,
    val wasSuccessful: Boolean,
    val details: String // Error details or notes
)
