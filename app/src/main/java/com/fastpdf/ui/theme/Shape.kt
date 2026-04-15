package com.fastpdf.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * FastPDF Shape System
 * Consistent rounded corners matching the reference UI.
 */
val FastPDFShapes = Shapes(
    // Chips, small badges
    extraSmall = RoundedCornerShape(4.dp),

    // Buttons, text fields
    small = RoundedCornerShape(8.dp),

    // Cards, tool items
    medium = RoundedCornerShape(12.dp),

    // Bottom sheets, dialogs
    large = RoundedCornerShape(16.dp),

    // Full-screen modals
    extraLarge = RoundedCornerShape(24.dp)
)
