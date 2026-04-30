package com.fastpdf.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Room Database for FastPDF.
 * Stores file access history, favorites, and metadata.
 */
@Database(
    entities = [FileHistoryEntity::class],
    version = 2,
    exportSchema = false
)
abstract class FastPdfDatabase : RoomDatabase() {

    abstract fun fileHistoryDao(): FileHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: FastPdfDatabase? = null

        fun getInstance(context: Context): FastPdfDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FastPdfDatabase::class.java,
                    "fastpdf_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
