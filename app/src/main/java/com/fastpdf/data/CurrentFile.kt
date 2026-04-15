package com.fastpdf.data

import android.net.Uri
import com.fastpdf.domain.model.DocumentType

/**
 * Singleton holding the currently selected file for viewing.
 *
 * Why a singleton? Navigation Compose has issues passing URIs as arguments
 * (encoding/decoding problems). This is the recommended lightweight pattern
 * for sharing transient state between screens without a ViewModel dependency.
 *
 * Will be replaced by proper DI (Hilt) + ViewModel in Phase 3.
 */
object CurrentFile {
    var uri: Uri? = null
    var name: String = ""
    var mimeType: String = ""
    var type: DocumentType = DocumentType.OTHER
    var sizeBytes: Long = 0L

    fun set(uri: Uri, name: String, mimeType: String, sizeBytes: Long = 0L) {
        this.uri = uri
        this.name = name
        this.mimeType = mimeType
        this.type = resolveType(name, mimeType)
        this.sizeBytes = sizeBytes
    }

    fun clear() {
        uri = null
        name = ""
        mimeType = ""
        type = DocumentType.OTHER
        sizeBytes = 0L
    }

    private fun resolveType(fileName: String, mimeType: String): DocumentType {
        // Try extension first, then mime type
        val fromExtension = DocumentType.fromFileName(fileName)
        if (fromExtension != DocumentType.OTHER) return fromExtension

        return when {
            mimeType.contains("pdf") -> DocumentType.PDF
            mimeType.contains("word") || mimeType.contains("document") -> DocumentType.WORD
            mimeType.contains("excel") || mimeType.contains("sheet") || mimeType.contains("csv") -> DocumentType.EXCEL
            mimeType.contains("powerpoint") || mimeType.contains("presentation") -> DocumentType.POWERPOINT
            mimeType.startsWith("image/") -> DocumentType.IMAGE
            mimeType.startsWith("text/") -> DocumentType.TEXT
            else -> DocumentType.OTHER
        }
    }
}
