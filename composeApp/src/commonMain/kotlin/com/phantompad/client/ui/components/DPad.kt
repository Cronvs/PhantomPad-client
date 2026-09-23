package com.phantompad.client.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.phantompad.client.ui.theme.*
import kotlin.math.abs

/**
 * A cross-shaped directional pad with 4 directions.
 * Supports diagonals when finger is between two directions.
 *
 * @param onDirectionChanged Called with (up, down, left, right) booleans.
 */
@Composable
fun DPad(
    modifier: Modifier = Modifier,
    size: Dp = 130.dp,
    onDirectionChanged: (up: Boolean, down: Boolean, left: Boolean, right: Boolean) -> Unit,
) {
    var pressedUp by remember { mutableStateOf(false) }
    var pressedDown by remember { mutableStateOf(false) }
    var pressedLeft by remember { mutableStateOf(false) }
    var pressedRight by remember { mutableStateOf(false) }

    Canvas(
        modifier = modifier
            .size(size)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val isPressed = event.changes.any { it.pressed }

                        if (isPressed) {
                            val pos = event.changes.first().position
                            val cx = this.size.width / 2f
                            val cy = this.size.height / 2f

                            // Normalize to -1..1
                            val nx = (pos.x - cx) / cx
                            val ny = (pos.y - cy) / cy

                            // Determine direction with diagonal support
                            val threshold = 0.2f
                            val newUp = ny < -threshold
                            val newDown = ny > threshold
                            val newLeft = nx < -threshold
                            val newRight = nx > threshold

                            if (newUp != pressedUp || newDown != pressedDown ||
                                newLeft != pressedLeft || newRight != pressedRight
                            ) {
                                pressedUp = newUp
                                pressedDown = newDown
                                pressedLeft = newLeft
                                pressedRight = newRight
                                onDirectionChanged(newUp, newDown, newLeft, newRight)
                            }
                        } else {
                            if (pressedUp || pressedDown || pressedLeft || pressedRight) {
                                pressedUp = false
                                pressedDown = false
                                pressedLeft = false
                                pressedRight = false
                                onDirectionChanged(false, false, false, false)
                            }
                        }

                        event.changes.forEach { it.consume() }
                    }
                }
            }
    ) {
        val totalSize = this.size.minDimension
        val armWidth = totalSize * 0.36f
        val cx = center.x
        val cy = center.y
        val half = totalSize / 2f
        val halfArm = armWidth / 2f
        val cornerR = armWidth * 0.15f

        // Draw cross shape background
        val crossPath = Path().apply {
            // Vertical arm
            addRoundRect(
                androidx.compose.ui.geometry.RoundRect(
                    left = cx - halfArm,
                    top = cy - half + 4f,
                    right = cx + halfArm,
                    bottom = cy + half - 4f,
                    radiusX = cornerR,
                    radiusY = cornerR,
                )
            )
            // Horizontal arm
            addRoundRect(
                androidx.compose.ui.geometry.RoundRect(
                    left = cx - half + 4f,
                    top = cy - halfArm,
                    right = cx + half - 4f,
                    bottom = cy + halfArm,
                    radiusX = cornerR,
                    radiusY = cornerR,
                )
            )
        }

        drawPath(crossPath, color = DarkSurfaceVariant)
        drawPath(crossPath, color = ButtonBorder, style = Stroke(width = 1.5f))

        // Draw direction highlights
        val highlightAlpha = 0.5f

        // Up
        if (pressedUp) {
            drawRoundRect(
                color = AccentPurple.copy(alpha = highlightAlpha),
                topLeft = Offset(cx - halfArm + 2f, cy - half + 6f),
                size = Size(armWidth - 4f, half - halfArm - 4f),
                cornerRadius = CornerRadius(cornerR),
            )
        }

        // Down
        if (pressedDown) {
            drawRoundRect(
                color = AccentPurple.copy(alpha = highlightAlpha),
                topLeft = Offset(cx - halfArm + 2f, cy + halfArm + 2f),
                size = Size(armWidth - 4f, half - halfArm - 6f),
                cornerRadius = CornerRadius(cornerR),
            )
        }

        // Left
        if (pressedLeft) {
            drawRoundRect(
                color = AccentPurple.copy(alpha = highlightAlpha),
                topLeft = Offset(cx - half + 6f, cy - halfArm + 2f),
                size = Size(half - halfArm - 4f, armWidth - 4f),
                cornerRadius = CornerRadius(cornerR),
            )
        }

        // Right
        if (pressedRight) {
            drawRoundRect(
                color = AccentPurple.copy(alpha = highlightAlpha),
                topLeft = Offset(cx + halfArm + 2f, cy - halfArm + 2f),
                size = Size(half - halfArm - 6f, armWidth - 4f),
                cornerRadius = CornerRadius(cornerR),
            )
        }

        // Direction arrows
        val arrowColor = DarkOnSurfaceVariant.copy(alpha = 0.6f)
        val arrowSize = armWidth * 0.22f

        // Up arrow
        drawArrow(cx, cy - half * 0.6f, arrowSize, Direction.UP, arrowColor)
        // Down arrow
        drawArrow(cx, cy + half * 0.6f, arrowSize, Direction.DOWN, arrowColor)
        // Left arrow
        drawArrow(cx - half * 0.6f, cy, arrowSize, Direction.LEFT, arrowColor)
        // Right arrow
        drawArrow(cx + half * 0.6f, cy, arrowSize, Direction.RIGHT, arrowColor)
    }
}

private enum class Direction { UP, DOWN, LEFT, RIGHT }

private fun DrawScope.drawArrow(cx: Float, cy: Float, size: Float, dir: Direction, color: Color) {
    val path = Path()
    when (dir) {
        Direction.UP -> {
            path.moveTo(cx, cy - size)
            path.lineTo(cx - size, cy + size * 0.3f)
            path.lineTo(cx + size, cy + size * 0.3f)
            path.close()
        }
        Direction.DOWN -> {
            path.moveTo(cx, cy + size)
            path.lineTo(cx - size, cy - size * 0.3f)
            path.lineTo(cx + size, cy - size * 0.3f)
            path.close()
        }
        Direction.LEFT -> {
            path.moveTo(cx - size, cy)
            path.lineTo(cx + size * 0.3f, cy - size)
            path.lineTo(cx + size * 0.3f, cy + size)
            path.close()
        }
        Direction.RIGHT -> {
            path.moveTo(cx + size, cy)
            path.lineTo(cx - size * 0.3f, cy - size)
            path.lineTo(cx - size * 0.3f, cy + size)
            path.close()
        }
    }
    drawPath(path, color)
}
