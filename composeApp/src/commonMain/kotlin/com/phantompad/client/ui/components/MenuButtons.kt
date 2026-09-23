package com.phantompad.client.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.phantompad.client.ui.theme.*

/**
 * Menu button row: Back (View), Guide (Xbox), Start (Menu).
 * Small circular/pill buttons centered in the controller layout.
 */
@Composable
fun MenuButtons(
    modifier: Modifier = Modifier,
    onBackChanged: (Boolean) -> Unit,
    onGuideChanged: (Boolean) -> Unit,
    onStartChanged: (Boolean) -> Unit,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Back / View button
        SmallMenuButton(
            modifier = Modifier.size(32.dp),
            onPressChanged = onBackChanged,
        ) {
            val w = size.width
            val h = size.height
            val iconColor = DarkOnSurfaceVariant
            val rectSize = w * 0.22f
            // Two overlapping rectangles icon
            drawRect(iconColor, Offset(w * 0.28f, h * 0.32f), Size(rectSize, rectSize), style = Stroke(1.5f))
            drawRect(iconColor, Offset(w * 0.45f, h * 0.45f), Size(rectSize, rectSize), style = Stroke(1.5f))
        }

        // Guide / Xbox button (larger)
        SmallMenuButton(
            modifier = Modifier.size(38.dp),
            isGuide = true,
            onPressChanged = onGuideChanged,
        ) {
            val cx = center.x
            val cy = center.y
            val r = size.minDimension * 0.22f
            val iconColor = AccentPurple.copy(alpha = 0.7f)
            // Simple "X" in center for Xbox guide
            drawLine(iconColor, Offset(cx - r, cy - r), Offset(cx + r, cy + r), 2.5f)
            drawLine(iconColor, Offset(cx + r, cy - r), Offset(cx - r, cy + r), 2.5f)
        }

        // Start / Menu button
        SmallMenuButton(
            modifier = Modifier.size(32.dp),
            onPressChanged = onStartChanged,
        ) {
            val w = size.width
            val h = size.height
            val iconColor = DarkOnSurfaceVariant
            val lineW = w * 0.35f
            val startX = (w - lineW) / 2f
            // Three horizontal lines (hamburger)
            drawLine(iconColor, Offset(startX, h * 0.35f), Offset(startX + lineW, h * 0.35f), 2f)
            drawLine(iconColor, Offset(startX, h * 0.5f), Offset(startX + lineW, h * 0.5f), 2f)
            drawLine(iconColor, Offset(startX, h * 0.65f), Offset(startX + lineW, h * 0.65f), 2f)
        }
    }
}

@Composable
private fun SmallMenuButton(
    modifier: Modifier = Modifier,
    isGuide: Boolean = false,
    onPressChanged: (Boolean) -> Unit,
    drawIcon: androidx.compose.ui.graphics.drawscope.DrawScope.() -> Unit,
) {
    var isPressed by remember { mutableStateOf(false) }

    Canvas(
        modifier = modifier
            .clip(CircleShape)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val pressed = event.changes.any { it.pressed }
                        if (pressed != isPressed) {
                            isPressed = pressed
                            onPressChanged(pressed)
                        }
                        event.changes.forEach { it.consume() }
                    }
                }
            }
    ) {
        val r = size.minDimension / 2f

        // Background
        drawCircle(
            color = if (isPressed) AccentPurple.copy(alpha = 0.4f)
            else if (isGuide) DarkSurfaceVariant
            else ButtonDefault.copy(alpha = 0.6f),
            radius = r,
        )

        // Border
        drawCircle(
            color = if (isPressed) AccentPurple
            else if (isGuide) AccentPurple.copy(alpha = 0.3f)
            else ButtonBorder.copy(alpha = 0.5f),
            radius = r,
            style = Stroke(width = 1.5f),
        )

        // Icon
        drawIcon()
    }
}
