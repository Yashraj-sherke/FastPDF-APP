package com.fastpdf.ui.editor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Approval
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fastpdf.domain.model.EditTool
import com.fastpdf.ui.theme.Primary

/**
 * Floating annotation toolbar for document editing.
 *
 * Features:
 * - Tool selection (Pen, Highlighter, Text, Signature, Stamp)
 * - Color picker (8 preset colors)
 * - Undo/Redo buttons
 * - Animated show/hide
 * - Active tool highlighting
 */
@Composable
fun AnnotationToolbar(
    isVisible: Boolean,
    activeTool: EditTool,
    activeColor: Color,
    onToolSelect: (EditTool) -> Unit,
    onColorSelect: (Color) -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showColorPicker by remember { mutableStateOf(false) }

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Color picker panel (expandable)
            AnimatedVisibility(visible = showColorPicker) {
                ColorPickerRow(
                    activeColor = activeColor,
                    onColorSelect = { color ->
                        onColorSelect(color)
                        showColorPicker = false
                    }
                )
            }

            // Main toolbar
            Row(
                modifier = Modifier
                    .shadow(8.dp, RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pen
                ToolButton(
                    icon = Icons.Filled.Draw,
                    label = "Pen",
                    isActive = activeTool == EditTool.PEN,
                    onClick = { onToolSelect(if (activeTool == EditTool.PEN) EditTool.NONE else EditTool.PEN) }
                )

                // Highlighter
                ToolButton(
                    icon = Icons.Filled.Brush,
                    label = "Highlight",
                    isActive = activeTool == EditTool.HIGHLIGHTER,
                    onClick = { onToolSelect(if (activeTool == EditTool.HIGHLIGHTER) EditTool.NONE else EditTool.HIGHLIGHTER) }
                )

                // Text
                ToolButton(
                    icon = Icons.Filled.TextFields,
                    label = "Text",
                    isActive = activeTool == EditTool.TEXT,
                    onClick = { onToolSelect(if (activeTool == EditTool.TEXT) EditTool.NONE else EditTool.TEXT) }
                )

                // Stamp
                ToolButton(
                    icon = Icons.Filled.Approval,
                    label = "Stamp",
                    isActive = activeTool == EditTool.STAMP,
                    onClick = { onToolSelect(if (activeTool == EditTool.STAMP) EditTool.NONE else EditTool.STAMP) }
                )

                // Divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(28.dp)
                        .background(Color.Gray.copy(alpha = 0.3f))
                )

                // Color selector
                IconButton(
                    onClick = { showColorPicker = !showColorPicker },
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(activeColor)
                            .border(2.dp, Color.Gray.copy(alpha = 0.3f), CircleShape)
                    )
                }

                // Undo
                IconButton(onClick = onUndo, modifier = Modifier.size(40.dp)) {
                    Icon(
                        Icons.Filled.Undo, contentDescription = "Undo",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Redo
                IconButton(onClick = onRedo, modifier = Modifier.size(40.dp)) {
                    Icon(
                        Icons.Filled.Redo, contentDescription = "Redo",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Close
                IconButton(onClick = onClose, modifier = Modifier.size(40.dp)) {
                    Icon(
                        Icons.Filled.Close, contentDescription = "Close Editor",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ToolButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .then(
                if (isActive) Modifier.background(Primary.copy(alpha = 0.1f))
                else Modifier
            )
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isActive) Primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            color = if (isActive) Primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ColorPickerRow(
    activeColor: Color,
    onColorSelect: (Color) -> Unit
) {
    val colors = listOf(
        Color.Black,
        Color(0xFFEF4444), // Red
        Color(0xFF4A6CF7), // Blue
        Color(0xFF10B981), // Green
        Color(0xFFFF8C42), // Orange
        Color(0xFF7B1FA2), // Purple
        Color(0xFFFFD700), // Yellow
        Color(0xFFFF69B4)  // Pink
    )

    Row(
        modifier = Modifier
            .padding(bottom = 8.dp)
            .shadow(4.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        colors.forEach { color ->
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(color)
                    .then(
                        if (color == activeColor)
                            Modifier.border(3.dp, Primary, CircleShape)
                        else
                            Modifier.border(1.dp, Color.Gray.copy(alpha = 0.3f), CircleShape)
                    )
                    .clickable { onColorSelect(color) }
            )
        }
    }
}
