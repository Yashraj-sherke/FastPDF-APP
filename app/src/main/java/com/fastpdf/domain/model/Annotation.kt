package com.fastpdf.domain.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

/**
 * Annotation data models for document editing.
 *
 * Supports:
 * - Freehand drawing paths
 * - Text notes
 * - Highlights
 * - Signatures
 * - Stamps
 */

/** A single drawing stroke (series of points with color + thickness). */
data class DrawingStroke(
    val points: List<Offset>,
    val color: Color,
    val strokeWidth: Float,
    val isHighlight: Boolean = false
)

/** A text note placed at a specific position. */
data class TextNote(
    val text: String,
    val position: Offset,
    val color: Color = Color.Black,
    val fontSize: Float = 14f
)

/** Available editing tools. */
enum class EditTool {
    NONE,
    PEN,
    HIGHLIGHTER,
    ERASER,
    TEXT,
    SIGNATURE,
    STAMP
}

/** Predefined stamp types. */
enum class StampType(val label: String, val color: Color) {
    APPROVED("APPROVED", Color(0xFF10B981)),
    REJECTED("REJECTED", Color(0xFFEF4444)),
    CONFIDENTIAL("CONFIDENTIAL", Color(0xFFEF4444)),
    DRAFT("DRAFT", Color(0xFFFF8C42)),
    REVIEWED("REVIEWED", Color(0xFF4A6CF7)),
    FINAL("FINAL", Color(0xFF10B981))
}
