package com.fastpdf.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity for persisting file access history.
 * Tracks recently opened files, favorites, and metadata.
 */
@Entity(tableName = "file_history")
data class FileHistoryEntity(
    @PrimaryKey
    val uriString: String,

    val fileName: String,
    val mimeType: String,
    val fileSize: Long = 0L,
    val documentType: String = "OTHER", // DocumentType enum name

    /** Timestamp of last access (epoch millis) */
    val lastAccessedAt: Long = System.currentTimeMillis(),

    /** Timestamp of first access (epoch millis) */
    val firstAccessedAt: Long = System.currentTimeMillis(),

    /** Number of times this file has been opened */
    val accessCount: Int = 1,

    /** Whether user has marked this as favorite */
    val isFavorite: Boolean = false,

    /** Whether the file has been soft-deleted (in Recycle Bin) */
    val isDeleted: Boolean = false,

    /** Timestamp when file was moved to Recycle Bin (epoch millis). Null if not deleted. */
    val deletedAt: Long? = null
)
