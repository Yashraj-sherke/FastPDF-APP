package com.fastpdf.data.db

import android.content.Context
import android.net.Uri
import com.fastpdf.domain.model.DocumentType
import kotlinx.coroutines.flow.Flow

/**
 * Repository layer for file history operations.
 * Abstracts Room DAO behind a clean API for UI consumption.
 *
 * Phase 9: Added soft delete, restore, batch operations, and recycle bin support.
 */
class FileRepository(context: Context) {

    private val dao = FastPdfDatabase.getInstance(context).fileHistoryDao()

    // ━━━ Recent Files ━━━

    /** Observe recent files (reactive). */
    val recentFiles: Flow<List<FileHistoryEntity>> = dao.getRecentFiles(20)

    /** Observe all files sorted by recent. */
    val allFiles: Flow<List<FileHistoryEntity>> = dao.getAllRecent()

    // ━━━ Favorites ━━━

    /** Observe favorite files (reactive). */
    val favorites: Flow<List<FileHistoryEntity>> = dao.getFavorites()

    /** Toggle favorite status for a file. */
    suspend fun toggleFavorite(uriString: String) {
        dao.toggleFavorite(uriString)
    }

    // ━━━ Search ━━━

    /** Search files by name. */
    fun searchFiles(query: String): Flow<List<FileHistoryEntity>> = dao.searchFiles(query)

    // ━━━ File Access Tracking ━━━

    /**
     * Record that a file was opened.
     * Updates access time and count if already tracked, otherwise creates new entry.
     */
    suspend fun recordFileAccess(
        uri: Uri,
        fileName: String,
        mimeType: String,
        fileSize: Long,
        documentType: DocumentType
    ) {
        val uriString = uri.toString()
        val existing = dao.getByUri(uriString)

        if (existing != null) {
            dao.update(
                existing.copy(
                    fileName = fileName,
                    mimeType = mimeType,
                    fileSize = fileSize,
                    documentType = documentType.name,
                    lastAccessedAt = System.currentTimeMillis(),
                    accessCount = existing.accessCount + 1,
                    // If it was in recycle bin and user opens it again, restore it
                    isDeleted = false,
                    deletedAt = null
                )
            )
        } else {
            dao.insert(
                FileHistoryEntity(
                    uriString = uriString,
                    fileName = fileName,
                    mimeType = mimeType,
                    fileSize = fileSize,
                    documentType = documentType.name,
                    lastAccessedAt = System.currentTimeMillis(),
                    firstAccessedAt = System.currentTimeMillis(),
                    accessCount = 1,
                    isFavorite = false
                )
            )
        }
    }

    // ━━━ Stats ━━━

    val fileCount: Flow<Int> = dao.getFileCount()
    val totalSize: Flow<Long> = dao.getTotalSize()

    // ━━━ Recycle Bin (Phase 9) ━━━

    /** Observe deleted files. */
    val deletedFiles: Flow<List<FileHistoryEntity>> = dao.getDeletedFiles()

    /** Observe deleted file count. */
    val deletedCount: Flow<Int> = dao.getDeletedCount()

    /** Soft-delete a file (move to Recycle Bin). */
    suspend fun softDelete(uriString: String) {
        dao.softDelete(uriString, System.currentTimeMillis())
    }

    /** Restore a file from Recycle Bin. */
    suspend fun restore(uriString: String) {
        dao.restore(uriString)
    }

    /** Restore all files from Recycle Bin. */
    suspend fun restoreAll() {
        dao.restoreAll()
    }

    /** Permanently delete a single file. */
    suspend fun permanentDelete(uriString: String) {
        dao.delete(uriString)
    }

    /** Empty the entire Recycle Bin. */
    suspend fun emptyRecycleBin() {
        dao.emptyRecycleBin()
    }

    /** Auto-purge files deleted more than 30 days ago. */
    suspend fun autoPurge() {
        val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
        dao.purgeOlderThan(thirtyDaysAgo)
    }

    // ━━━ Batch Operations (Phase 9) ━━━

    /** Soft-delete multiple files. */
    suspend fun batchSoftDelete(uris: List<String>) {
        dao.batchSoftDelete(uris, System.currentTimeMillis())
    }

    /** Favorite multiple files. */
    suspend fun batchFavorite(uris: List<String>) {
        dao.batchFavorite(uris)
    }

    /** Unfavorite multiple files. */
    suspend fun batchUnfavorite(uris: List<String>) {
        dao.batchUnfavorite(uris)
    }

    // ━━━ Cleanup ━━━

    suspend fun deleteFile(uriString: String) = dao.delete(uriString)
    suspend fun clearHistory() = dao.clearAll()

    // ━━━ Storage Analytics (Phase 10) ━━━

    /** Storage breakdown per document type. */
    val storageByType: Flow<List<StorageByType>> = dao.getStorageByType()

    /** Most frequently opened files. */
    val mostOpened: Flow<List<FileHistoryEntity>> = dao.getMostOpened(5)
}
