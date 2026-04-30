package com.fastpdf.domain.model

import android.net.Uri

/**
 * Result types for PDF tool operations.
 * Used across all tool screens for consistent progress/result handling.
 */
sealed class ToolResult {
    /** Operation completed successfully. */
    data class Success(
        val outputUri: Uri,
        val message: String,
        val originalSizeBytes: Long = 0L,
        val outputSizeBytes: Long = 0L
    ) : ToolResult()

    /** Operation failed. */
    data class Error(val message: String) : ToolResult()

    /** Operation in progress. */
    data class Progress(val percent: Float, val message: String) : ToolResult()
}
