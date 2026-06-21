package com.example.stego.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [StegoHistory::class], version = 1, exportSchema = false)
abstract class StegoDatabase : RoomDatabase() {
    abstract fun stegoHistoryDao(): StegoHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: StegoDatabase? = null

        fun getDatabase(context: Context): StegoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StegoDatabase::class.java,
                    "stego_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
