package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.unit.dp
import com.example.data.model.HandwritingStroke
import com.example.data.model.PointData

@Composable
fun HandwritingBlockView(
    strokes: List<HandwritingStroke>,
    onStrokesChanged: (List<HandwritingStroke>) -> Unit,
    modifier: Modifier = Modifier,
    isEditable: Boolean = true
) {
    var currentColor by remember { mutableLongStateOf(0xFF000000) }
    var currentWidth by remember { mutableFloatStateOf(4f) }

    val currentStrokePoints = remember { mutableStateListOf<Offset>() }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
            .background(Color.White)
            .padding(8.dp)
    ) {
        if (isEditable) {
            // Handwriting Toolbar: Colors + Widths + Undo/Clear
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Color swatches
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val colors = listOf(0xFF000000, 0xFF1976D2, 0xFFD32F2F, 0xFF388E3C, 0xFFFFA000)
                    colors.forEach { c ->
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(c))
                                .border(
                                    if (currentColor == c) 2.dp else 1.dp,
                                    if (currentColor == c) MaterialTheme.colorScheme.primary else Color.LightGray,
                                    CircleShape
                                )
                                .clickable { currentColor = c }
                        )
                    }
                }

                // Stroke width chips
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    val widths = listOf(2f, 4f, 8f)
                    widths.forEach { w ->
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (currentWidth == w) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                            modifier = Modifier.clickable { currentWidth = w }
                        ) {
                            Text(
                                text = "${w.toInt()}pt",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                color = if (currentWidth == w) MaterialTheme.colorScheme.onPrimaryContainer else Color.Gray
                            )
                        }
                    }
                }

                // Actions: Undo & Clear
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            if (strokes.isNotEmpty()) {
                                onStrokesChanged(strokes.dropLast(1))
                            }
                        },
                        enabled = strokes.isNotEmpty(),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Undo,
                            contentDescription = "Undo stroke",
                            tint = if (strokes.isNotEmpty()) MaterialTheme.colorScheme.primary else Color.LightGray,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = { onStrokesChanged(emptyList()) },
                        enabled = strokes.isNotEmpty(),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear canvas",
                            tint = if (strokes.isNotEmpty()) Color.Red else Color.LightGray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // The Drawing Canvas
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color(0xFFFAFAFA), RoundedCornerShape(8.dp))
                .clip(RoundedCornerShape(8.dp))
                .then(
                    if (isEditable) {
                        Modifier.pointerInput(strokes) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    currentStrokePoints.clear()
                                    currentStrokePoints.add(offset)
                                },
                                onDrag = { change, _ ->
                                    change.consume()
                                    currentStrokePoints.add(change.position)
                                },
                                onDragEnd = {
                                    if (currentStrokePoints.size > 1) {
                                        val newStroke = HandwritingStroke(
                                            color = currentColor,
                                            width = currentWidth,
                                            points = currentStrokePoints.map { PointData(it.x, it.y) }
                                        )
                                        onStrokesChanged(strokes + newStroke)
                                    }
                                    currentStrokePoints.clear()
                                },
                                onDragCancel = {
                                    currentStrokePoints.clear()
                                }
                            )
                        }
                    } else Modifier
                )
        ) {
            // Draw completed strokes
            for (stroke in strokes) {
                if (stroke.points.size > 1) {
                    val path = Path().apply {
                        moveTo(stroke.points[0].x, stroke.points[0].y)
                        for (i in 1 until stroke.points.size) {
                            lineTo(stroke.points[i].x, stroke.points[i].y)
                        }
                    }
                    drawPath(
                        path = path,
                        color = Color(stroke.color),
                        style = Stroke(
                            width = stroke.width,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
            }

            // Draw current in-progress stroke
            if (currentStrokePoints.size > 1) {
                val activePath = Path().apply {
                    moveTo(currentStrokePoints[0].x, currentStrokePoints[0].y)
                    for (i in 1 until currentStrokePoints.size) {
                        lineTo(currentStrokePoints[i].x, currentStrokePoints[i].y)
                    }
                }
                drawPath(
                    path = activePath,
                    color = Color(currentColor),
                    style = Stroke(
                        width = currentWidth,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }
        }
    }
}
