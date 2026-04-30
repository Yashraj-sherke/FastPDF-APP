package com.fastpdf.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for file history operations.
 * All queries return Flows for reactive UI updates.
 *
 * Phase 9: Added soft-delete (Recycle Bin), batch operations,
 * and recycle bin auto-purge queries.
 */
@Dao
interface FileHistoryDao {

    // ━━━ Recent Files ━━━

    /** Get all files sorted by most recently accessed (excludes deleted). */
    @Query("SELECT * FROM file_history WHERE isDeleted = 0 ORDER BY lastAccessedAt DESC")
    fun getAllRecent(): Flow<List<FileHistoryEntity>>

    /** Get the N most recently accessed files (excludes deleted). */
    @Query("SELECT * FROM file_history WHERE isDeleted = 0 ORDER BY lastAccessedAt DESC LIMIT :limit")
    fun getRecentFiles(limit: Int = 10): Flow<List<FileHistoryEntity>>

    // ━━━ Favorites ━━━

    /** Get all favorite files (excludes deleted). */
    @Query("SELECT * FROM file_history WHERE isFavorite = 1 AND isDeleted = 0 ORDER BY lastAccessedAt DESC")
    fun getFavorites(): Flow<List<FileHistoryEntity>>

    /** Toggle favorite status. */
    @Query("UPDATE file_history SET isFavorite = NOT isFavorite WHERE uriString = :uriString")
    suspend fun toggleFavorite(uriString: String)

    // ━━━ Search ━━━

    /** Search files by name (case-insensitive, excludes deleted). */
    @Query("SELECT * FROM file_history WHERE isDeleted = 0 AND fileName LIKE '%' || :query || '%' ORDER BY lastAccessedAt DESC")
    fun searchFiles(query: String): Flow<List<FileHistoryEntity>>

    // ━━━ Insert/Update ━━━

    /** Insert a new file entry, or replace if URI already exists. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(file: FileHistoryEntity)

    /** Update existing file entry. */
    @Update
    suspend fun update(file: FileHistoryEntity)

    /** Get a single file by URI. */
    @Query("SELECT * FROM file_history WHERE uriString = :uriString LIMIT 1")
    suspend fun getByUri(uriString: String): FileHistoryEntity?

    // ━━━ Stats (excludes deleted) ━━━

    /** Total number of tracked files. */
    @Query("SELECT COUNT(*) FROM file_history WHERE isDeleted = 0")
    fun getFileCount(): Flow<Int>

    /** Total size of all tracked files. */
    @Query("SELECT COALESCE(SUM(fileSize), 0) FROM file_history WHERE isDeleted = 0")
    fun getTotalSize(): Flow<Long>

    // ━━━ Recycle Bin (Phase 9) ━━━

    /** Get all soft-deleted files, newest deletions first. */
    @Query("SELECT * FROM file_history WHERE isDeleted = 1 ORDER BY deletedAt DESC")
    fun getDeletedFiles(): Flow<List<FileHistoryEntity>>

    /** Count of files in Recycle Bin. */
    @Query("SELECT COUNT(*) FROM file_history WHERE isDeleted = 1")
    fun getDeletedCount(): Flow<Int>

    /** Soft-delete a single file (move to Recycle Bin). */
    @Query("UPDATE file_history SET isDeleted = 1, deletedAt = :timestamp WHERE uriString = :uriString")
    suspend fun softDelete(uriString: String, timestamp: Long = System.currentTimeMillis())

    /** Restore a soft-deleted file. */
    @Query("UPDATE file_history SET isDeleted = 0, deletedAt = NULL WHERE uriString = :uriString")
    suspend fun restore(uriString: String)

    /** Restore all files from Recycle Bin. */
    @Query("UPDATE file_history SET isDeleted = 0, deletedAt = NULL WHERE isDeleted = 1")
    suspend fun restoreAll()

    /** Permanently delete a single file. */
    @Query("DELETE FROM file_history WHERE uriString = :uriString")
    suspend fun delete(uriString: String)

    /** Permanently delete all files in the Recycle Bin. */
    @Query("DELETE FROM file_history WHERE isDeleted = 1")
    suspend fun emptyRecycleBin()

    /** Auto-purge: permanently delete files older than a threshold. */
    @Query("DELETE FROM file_history WHERE isDeleted = 1 AND deletedAt < :threshold")
    suspend fun purgeOlderThan(threshold: Long)

    // ━━━ Batch Operations (Phase 9) ━━━

    /** Soft-delete multiple files at once. */
    @Query("UPDATE file_history SET isDeleted = 1, deletedAt = :timestamp WHERE uriString IN (:uris)")
    suspend fun batchSoftDelete(uris: List<String>, timestamp: Long = System.currentTimeMillis())

    /** Favorite multiple files at once. */
    @Query("UPDATE file_history SET isFavorite = 1 WHERE uriString IN (:uris)")
    suspend fun batchFavorite(uris: List<String>)

    /** Unfavorite multiple files at once. */
    @Query("UPDATE file_history SET isFavorite = 0 WHERE uriString IN (:uris)")
    suspend fun batchUnfavorite(uris: List<String>)

    // ━━━ Cleanup ━━━

    /** Clear all history. */
    @Query("DELETE FROM file_history")
    suspend fun clearAll()

    // ━━━ Storage Analytics (Phase 10) ━━━

    /** Get total size per document type (excludes deleted). */
    @Query("SELECT documentType, COALESCE(SUM(fileSize), 0) AS totalSize, COUNT(*) AS count FROM file_history WHERE isDeleted = 0 GROUP BY documentType")
    fun getStorageByType(): Flow<List<StorageByType>>

    /** Get most-opened files sorted by access count. */
    @Query("SELECT * FROM file_history WHERE isDeleted = 0 ORDER BY accessCount DESC LIMIT :limit")
    fun getMostOpened(limit: Int = 5): Flow<List<FileHistoryEntity>>
}
