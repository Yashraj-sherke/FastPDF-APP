package com.fastpdf.data.db

/**
 * POJO for Room query result — storage breakdown by document type.
 * Used by FileHistoryDao.getStorageByType().
 */
data class StorageByType(
    val documentType: String,
    val totalSize: Long,
    val count: Int
)
