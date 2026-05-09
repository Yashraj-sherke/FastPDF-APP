package com.fastpdf.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * FastPDF Shape System
 * Generous rounded corners matching the reference screenshot UI.
 */
val FastPDFShapes = Shapes(
    // Chips, small badges
    extraSmall = RoundedCornerShape(6.dp),

    // Buttons, text fields, search bars
    small = RoundedCornerShape(12.dp),

    // Cards, tool items, filter chips
    medium = RoundedCornerShape(16.dp),

    // Bottom sheets, dialogs, recently added card
    large = RoundedCornerShape(20.dp),

    // Full-screen modals, onboarding cards
    extraLarge = RoundedCornerShape(28.dp)
)
