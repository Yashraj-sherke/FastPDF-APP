package com.fastpdf.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Slideshow
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Supported document types in FastPDF.
 * Each type has an associated icon and accent color for visual distinction.
 */
enum class DocumentType(
    val icon: ImageVector,
    val tintColor: Color,
    val extensions: List<String>,
    val displayName: String
) {
    PDF(
        icon = Icons.Default.PictureAsPdf,
        tintColor = Color(0xFFE53935),
        extensions = listOf("pdf"),
        displayName = "PDF"
    ),
    WORD(
        icon = Icons.Default.Description,
        tintColor = Color(0xFF1565C0),
        extensions = listOf("doc", "docx"),
        displayName = "Word"
    ),
    EXCEL(
        icon = Icons.Default.TableChart,
        tintColor = Color(0xFF2E7D32),
        extensions = listOf("xls", "xlsx", "csv"),
        displayName = "Excel"
    ),
    POWERPOINT(
        icon = Icons.Default.Slideshow,
        tintColor = Color(0xFFE65100),
        extensions = listOf("ppt", "pptx"),
        displayName = "PowerPoint"
    ),
    IMAGE(
        icon = Icons.Default.Image,
        tintColor = Color(0xFF7B1FA2),
        extensions = listOf("jpg", "jpeg", "png", "webp", "gif"),
        displayName = "Image"
    ),
    TEXT(
        icon = Icons.Default.Description,
        tintColor = Color(0xFF455A64),
        extensions = listOf("txt", "md", "rtf"),
        displayName = "Text"
    ),
    OTHER(
        icon = Icons.Default.InsertDriveFile,
        tintColor = Color(0xFF6B7280),
        extensions = emptyList(),
        displayName = "File"
    );

    companion object {
        fun fromExtension(extension: String): DocumentType {
            val ext = extension.lowercase()
            return entries.find { ext in it.extensions } ?: OTHER
        }

        fun fromFileName(fileName: String): DocumentType {
            val ext = fileName.substringAfterLast('.', "")
            return fromExtension(ext)
        }
    }
}

/**
 * Represents a document/file in the app.
 * Immutable data class for Compose stability and performance.
 */
data class DocumentFile(
    val id: String,
    val name: String,
    val type: DocumentType,
    val sizeBytes: Long,
    val lastModified: String,
    val path: String = "",
    val isFavorite: Boolean = false
) {
    /** Human-readable file size */
    val displaySize: String
        get() = when {
            sizeBytes < 1024 -> "$sizeBytes B"
            sizeBytes < 1024 * 1024 -> "${sizeBytes / 1024} KB"
            sizeBytes < 1024 * 1024 * 1024 -> "%.1f MB".format(sizeBytes / (1024.0 * 1024.0))
            else -> "%.1f GB".format(sizeBytes / (1024.0 * 1024.0 * 1024.0))
        }
}
