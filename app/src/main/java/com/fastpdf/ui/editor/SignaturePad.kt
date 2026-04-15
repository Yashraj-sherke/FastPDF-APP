package com.fastpdf.ui.editor

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fastpdf.ui.theme.Primary

/**
 * E-Signature pad for drawing signatures.
 *
 * Features:
 * - Smooth freehand drawing
 * - Clear button to reset
 * - Done button to confirm
 * - White canvas with border
 * - Bezier curve smoothing for natural strokes
 */
@Composable
fun SignaturePad(
    onSignatureDone: (List<List<Offset>>) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strokes = remember { mutableStateListOf<List<Offset>>() }
    val currentStroke = remember { mutableStateListOf<Offset>() }
    var hasDrawn by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Draw Your Signature",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Signature canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .then(
                    Modifier.clip(RoundedCornerShape(12.dp))
                )
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                currentStroke.clear()
                                currentStroke.add(offset)
                                hasDrawn = true
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                currentStroke.add(change.position)
                            },
                            onDragEnd = {
                                if (currentStroke.size > 1) {
                                    strokes.add(currentStroke.toList())
                                }
                                currentStroke.clear()
                            }
                        )
                    }
            ) {
                // Border
                drawRect(
                    color = Color.Gray.copy(alpha = 0.3f),
                    style = Stroke(width = 2f)
                )

                // Signature line guide
                drawLine(
                    color = Color.Gray.copy(alpha = 0.2f),
                    start = Offset(40f, size.height * 0.7f),
                    end = Offset(size.width - 40f, size.height * 0.7f),
                    strokeWidth = 1f
                )

                // Draw existing strokes
                strokes.forEach { stroke ->
                    drawStrokePath(stroke)
                }

                // Draw current stroke
                if (currentStroke.size >= 2) {
                    drawStrokePath(currentStroke.toList())
                }
            }

            // "Sign here" hint
            if (!hasDrawn) {
                Text(
                    text = "Sign here",
                    color = Color.Gray.copy(alpha = 0.4f),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(bottom = 20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Cancel")
            }

            OutlinedButton(
                onClick = {
                    strokes.clear()
                    currentStroke.clear()
                    hasDrawn = false
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Clear")
            }

            Button(
                onClick = { onSignatureDone(strokes.toList()) },
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                enabled = strokes.isNotEmpty(),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Done", color = Color.White)
            }
        }
    }
}

/**
 * Extension to draw a smooth bezier path from a list of points.
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawStrokePath(
    points: List<Offset>
) {
    if (points.size < 2) return

    val path = Path().apply {
        moveTo(points[0].x, points[0].y)
        for (i in 1 until points.size) {
            val prev = points[i - 1]
            val curr = points[i]
            val mid = Offset((prev.x + curr.x) / 2f, (prev.y + curr.y) / 2f)
            quadraticBezierTo(prev.x, prev.y, mid.x, mid.y)
        }
        lineTo(points.last().x, points.last().y)
    }

    drawPath(
        path = path,
        color = Color(0xFF1A1C2E),
        style = Stroke(
            width = 3f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )
}
