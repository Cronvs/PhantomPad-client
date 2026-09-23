package com.phantompad.client.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.phantompad.client.ui.theme.*
import kotlin.math.sqrt

/**
 * Xbox-style A/B/X/Y face buttons in a diamond layout.
 * Supports multi-touch (pressing multiple buttons simultaneously).
 *
 *         Y
 *       X   B
 *         A
 */
@Composable
fun FaceButtons(
    modifier: Modifier = Modifier,
    size: Dp = 140.dp,
    onButtonChanged: (a: Boolean, b: Boolean, x: Boolean, y: Boolean) -> Unit,
) {
    var aPressed by remember { mutableStateOf(false) }
    var bPressed by remember { mutableStateOf(false) }
    var xPressed by remember { mutableStateOf(false) }
    var yPressed by remember { mutableStateOf(false) }

    Canvas(
        modifier = modifier
            .size(size)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()

                        val totalSize = this.size.width.toFloat()
                        val btnRadius = totalSize * 0.16f
                        val spread = totalSize * 0.28f
                        val cx = totalSize / 2f
                        val cy = totalSize / 2f

                        // Button centers in diamond layout
                        val aCenter = Offset(cx, cy + spread)         // bottom
                        val bCenter = Offset(cx + spread, cy)         // right
                        val xCenter = Offset(cx - spread, cy)         // left
                        val yCenter = Offset(cx, cy - spread)         // top

                        var newA = false
                        var newB = false
                        var newX = false
                        var newY = false

                        // Check all active pointers (multi-touch)
                        for (change in event.changes) {
                            if (change.pressed) {
                                val pos = change.position
                                if (distance(pos, aCenter) < btnRadius * 1.4f) newA = true
                                if (distance(pos, bCenter) < btnRadius * 1.4f) newB = true
                                if (distance(pos, xCenter) < btnRadius * 1.4f) newX = true
                                if (distance(pos, yCenter) < btnRadius * 1.4f) newY = true
                            }
                            change.consume()
                        }

                        if (newA != aPressed || newB != bPressed || newX != xPressed || newY != yPressed) {
                            aPressed = newA
                            bPressed = newB
                            xPressed = newX
                            yPressed = newY
                            onButtonChanged(newA, newB, newX, newY)
                        }
                    }
                }
            }
    ) {
        val totalSize = this.size.minDimension
        val btnRadius = totalSize * 0.16f
        val spread = totalSize * 0.28f
        val cx = center.x
        val cy = center.y

        // A button (bottom, green)
        drawFaceButton(Offset(cx, cy + spread), btnRadius, ButtonA, aPressed, "A")
        // B button (right, red)
        drawFaceButton(Offset(cx + spread, cy), btnRadius, ButtonB, bPressed, "B")
        // X button (left, blue)
        drawFaceButton(Offset(cx - spread, cy), btnRadius, ButtonX, xPressed, "X")
        // Y button (top, yellow)
        drawFaceButton(Offset(cx, cy - spread), btnRadius, ButtonY, yPressed, "Y")
    }
}

private fun DrawScope.drawFaceButton(
    center: Offset,
    radius: Float,
    color: Color,
    pressed: Boolean,
    label: String,
) {
    // Background
    val bgColor = if (pressed) color.copy(alpha = 0.4f) else ButtonDefault
    drawCircle(color = bgColor, radius = radius, center = center)

    // Border
    val borderColor = if (pressed) color else color.copy(alpha = 0.5f)
    drawCircle(
        color = borderColor,
        radius = radius,
        center = center,
        style = Stroke(width = if (pressed) 3f else 2f),
    )

    // Letter label - draw using simple path-based text
    val letterColor = if (pressed) Color.White else color.copy(alpha = 0.8f)
    val letterSize = radius * 0.65f

    // Simple letter rendering using lines
    drawLetterSimple(center, letterSize, label, letterColor)
}

/**
 * Draws simple block letters using lines - avoids needing platform text APIs.
 */
private fun DrawScope.drawLetterSimple(center: Offset, size: Float, letter: String, color: Color) {
    val strokeWidth = size * 0.25f
    val halfW = size * 0.4f
    val halfH = size * 0.5f
    val cx = center.x
    val cy = center.y

    when (letter) {
        "A" -> {
            // Two legs + crossbar
            drawLine(color, Offset(cx - halfW, cy + halfH), Offset(cx, cy - halfH), strokeWidth)
            drawLine(color, Offset(cx, cy - halfH), Offset(cx + halfW, cy + halfH), strokeWidth)
            drawLine(color, Offset(cx - halfW * 0.55f, cy + halfH * 0.1f), Offset(cx + halfW * 0.55f, cy + halfH * 0.1f), strokeWidth)
        }
        "B" -> {
            // Vertical + two bumps
            drawLine(color, Offset(cx - halfW * 0.4f, cy - halfH), Offset(cx - halfW * 0.4f, cy + halfH), strokeWidth)
            drawLine(color, Offset(cx - halfW * 0.4f, cy - halfH), Offset(cx + halfW * 0.3f, cy - halfH), strokeWidth)
            drawLine(color, Offset(cx + halfW * 0.3f, cy - halfH), Offset(cx + halfW * 0.5f, cy - halfH * 0.4f), strokeWidth)
            drawLine(color, Offset(cx + halfW * 0.5f, cy - halfH * 0.4f), Offset(cx - halfW * 0.1f, cy), strokeWidth)
            drawLine(color, Offset(cx - halfW * 0.4f, cy), Offset(cx + halfW * 0.3f, cy), strokeWidth)
            drawLine(color, Offset(cx + halfW * 0.3f, cy), Offset(cx + halfW * 0.5f, cy + halfH * 0.4f), strokeWidth)
            drawLine(color, Offset(cx + halfW * 0.5f, cy + halfH * 0.4f), Offset(cx - halfW * 0.4f, cy + halfH), strokeWidth)
        }
        "X" -> {
            drawLine(color, Offset(cx - halfW, cy - halfH), Offset(cx + halfW, cy + halfH), strokeWidth)
            drawLine(color, Offset(cx + halfW, cy - halfH), Offset(cx - halfW, cy + halfH), strokeWidth)
        }
        "Y" -> {
            drawLine(color, Offset(cx - halfW, cy - halfH), Offset(cx, cy), strokeWidth)
            drawLine(color, Offset(cx + halfW, cy - halfH), Offset(cx, cy), strokeWidth)
            drawLine(color, Offset(cx, cy), Offset(cx, cy + halfH), strokeWidth)
        }
    }
}

private fun distance(a: Offset, b: Offset): Float {
    val dx = a.x - b.x
    val dy = a.y - b.y
    return sqrt(dx * dx + dy * dy)
}
