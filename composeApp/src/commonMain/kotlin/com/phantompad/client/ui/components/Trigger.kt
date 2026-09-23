package com.phantompad.client.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.phantompad.client.ui.theme.*

/**
 * An analog trigger (LT / RT).
 * Vertical slider: drag up from bottom to engage. The fill level represents
 * the analog value from 0.0 (released) to 1.0 (fully pressed).
 */
@Composable
fun Trigger(
    modifier: Modifier = Modifier,
    width: Dp = 44.dp,
    height: Dp = 80.dp,
    label: String = "LT",
    onValueChanged: (Float) -> Unit,
) {
    var value by remember { mutableFloatStateOf(0f) }

    Canvas(
        modifier = modifier
            .width(width)
            .height(height)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val anyPressed = event.changes.any { it.pressed }

                        if (anyPressed) {
                            val pos = event.changes.first().position
                            // Map Y position to value: top=1.0, bottom=0.0
                            val h = this.size.height.toFloat()
                            val newValue = (1f - (pos.y / h)).coerceIn(0f, 1f)
                            if (newValue != value) {
                                value = newValue
                                onValueChanged(newValue)
                            }
                        } else {
                            if (value != 0f) {
                                value = 0f
                                onValueChanged(0f)
                            }
                        }

                        event.changes.forEach { it.consume() }
                    }
                }
            }
    ) {
        val w = this.size.width
        val h = this.size.height
        val cornerRadius = CornerRadius(w * 0.2f)
        val padding = 3f

        // Background track
        drawRoundRect(
            color = DarkSurfaceVariant,
            topLeft = Offset.Zero,
            size = Size(w, h),
            cornerRadius = cornerRadius,
        )

        // Fill from bottom
        val fillHeight = (h * value).coerceAtMost(h)
        if (fillHeight > 0) {
            drawRoundRect(
                color = AccentPurple.copy(alpha = 0.6f),
                topLeft = Offset(padding, h - fillHeight),
                size = Size(w - padding * 2, fillHeight - padding),
                cornerRadius = cornerRadius,
            )
        }

        // Border
        drawRoundRect(
            color = if (value > 0.05f) AccentPurple else ButtonBorder,
            topLeft = Offset.Zero,
            size = Size(w, h),
            cornerRadius = cornerRadius,
            style = Stroke(width = 1.5f),
        )

        // Label at bottom center
        val letterColor = if (value > 0.05f) Color.White else DarkOnSurfaceVariant
        val labelY = h * 0.85f
        val letterSize = w * 0.2f
        // Draw simple "L" or "R" + "T"
        if (label.startsWith("L")) {
            // L
            drawLine(letterColor, Offset(w * 0.28f, labelY - letterSize), Offset(w * 0.28f, labelY + letterSize * 0.3f), 2f)
            drawLine(letterColor, Offset(w * 0.28f, labelY + letterSize * 0.3f), Offset(w * 0.45f, labelY + letterSize * 0.3f), 2f)
        } else {
            // R
            drawLine(letterColor, Offset(w * 0.25f, labelY - letterSize), Offset(w * 0.25f, labelY + letterSize * 0.3f), 2f)
            drawLine(letterColor, Offset(w * 0.25f, labelY - letterSize), Offset(w * 0.42f, labelY - letterSize), 2f)
            drawLine(letterColor, Offset(w * 0.42f, labelY - letterSize), Offset(w * 0.42f, labelY - letterSize * 0.3f), 2f)
            drawLine(letterColor, Offset(w * 0.42f, labelY - letterSize * 0.3f), Offset(w * 0.25f, labelY), 2f)
        }
        // T
        drawLine(letterColor, Offset(w * 0.52f, labelY - letterSize), Offset(w * 0.72f, labelY - letterSize), 2f)
        drawLine(letterColor, Offset(w * 0.62f, labelY - letterSize), Offset(w * 0.62f, labelY + letterSize * 0.3f), 2f)
    }
}
