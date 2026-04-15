package com.fastpdf.ui.editor

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import com.fastpdf.domain.model.DrawingStroke
import com.fastpdf.domain.model.EditTool
import com.fastpdf.domain.model.TextNote

/**
 * Transparent drawing canvas overlay for document annotation.
 *
 * Features:
 * - Freehand pen drawing (variable color + width)
 * - Highlighter (semi-transparent, wide strokes)
 * - Text note placement on tap
 * - Eraser mode (removes strokes near touch)
 * - Renders all existing strokes and text notes
 *
 * This canvas is overlaid on top of the document viewer
 * and captures touch events only when an edit tool is active.
 */
@Composable
fun DrawingCanvas(
    strokes: List<DrawingStroke>,
    textNotes: List<TextNote>,
    activeTool: EditTool,
    activeColor: Color,
    onStrokeAdd: (DrawingStroke) -> Unit,
    onTextTap: (Offset) -> Unit,
    onEraseAt: (Offset) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDrawing = activeTool == EditTool.PEN || activeTool == EditTool.HIGHLIGHTER
    val isErasing = activeTool == EditTool.ERASER
    val isTextMode = activeTool == EditTool.TEXT

    val strokeWidth = when (activeTool) {
        EditTool.PEN -> 4f
        EditTool.HIGHLIGHTER -> 24f
        else -> 4f
    }
    val strokeColor = when (activeTool) {
        EditTool.HIGHLIGHTER -> activeColor.copy(alpha = 0.35f)
        else -> activeColor
    }
    val isHighlight = activeTool == EditTool.HIGHLIGHTER

    // Collect points for the current stroke being drawn
    val currentPoints = androidx.compose.runtime.remember { mutableListOf<Offset>() }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .then(
                if (isDrawing || isErasing) {
                    Modifier.pointerInput(activeTool, activeColor) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                currentPoints.clear()
                                currentPoints.add(offset)
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                currentPoints.add(change.position)
                            },
                            onDragEnd = {
                                if (isErasing) {
                                    currentPoints.forEach { point ->
                                        onEraseAt(point)
                                    }
                                } else if (currentPoints.size > 1) {
                                    onStrokeAdd(
                                        DrawingStroke(
                                            points = currentPoints.toList(),
                                            color = strokeColor,
                                            strokeWidth = strokeWidth,
                                            isHighlight = isHighlight
                                        )
                                    )
                                }
                                currentPoints.clear()
                            }
                        )
                    }
                } else if (isTextMode) {
                    Modifier.pointerInput(Unit) {
                        detectTapGestures { offset ->
                            onTextTap(offset)
                        }
                    }
                } else {
                    Modifier
                }
            )
    ) {
        // Draw existing strokes
        strokes.forEach { stroke ->
            if (stroke.points.size >= 2) {
                val path = Path().apply {
                    moveTo(stroke.points[0].x, stroke.points[0].y)
                    for (i in 1 until stroke.points.size) {
                        // Use quadratic bezier for smooth curves
                        val prev = stroke.points[i - 1]
                        val curr = stroke.points[i]
                        val mid = Offset(
                            (prev.x + curr.x) / 2f,
                            (prev.y + curr.y) / 2f
                        )
                        quadraticBezierTo(prev.x, prev.y, mid.x, mid.y)
                    }
                    val last = stroke.points.last()
                    lineTo(last.x, last.y)
                }

                drawPath(
                    path = path,
                    color = stroke.color,
                    style = Stroke(
                        width = stroke.strokeWidth,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }
        }

        // Draw current stroke being drawn (live preview)
        if (currentPoints.size >= 2 && isDrawing) {
            val path = Path().apply {
                moveTo(currentPoints[0].x, currentPoints[0].y)
                for (i in 1 until currentPoints.size) {
                    val prev = currentPoints[i - 1]
                    val curr = currentPoints[i]
                    val mid = Offset(
                        (prev.x + curr.x) / 2f,
                        (prev.y + curr.y) / 2f
                    )
                    quadraticBezierTo(prev.x, prev.y, mid.x, mid.y)
                }
                lineTo(currentPoints.last().x, currentPoints.last().y)
            }

            drawPath(
                path = path,
                color = strokeColor,
                style = Stroke(
                    width = strokeWidth,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }

        // Draw text notes
        textNotes.forEach { note ->
            drawCircle(
                color = note.color.copy(alpha = 0.15f),
                radius = 16f,
                center = note.position
            )
        }
    }
}
