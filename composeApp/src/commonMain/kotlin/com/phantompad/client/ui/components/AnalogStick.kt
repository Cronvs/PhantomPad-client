package com.phantompad.client.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.phantompad.client.ui.theme.*
import kotlin.math.sqrt

/**
 * A touch-tracked analog joystick.
 *
 * The thumb follows the user's finger within a circular boundary.
 * Returns normalized X,Y in -1.0..1.0 via the onValueChanged callback.
 * Springs back to center on release.
 *
 * @param size Total size of the component (outer ring diameter).
 * @param deadzone Inner fraction (0-1) where small movements are ignored.
 * @param onValueChanged Called with (x, y) normalized to -1.0..1.0.
 * @param onPress Called when the stick is clicked (thumb button).
 */
@Composable
fun AnalogStick(
    modifier: Modifier = Modifier,
    size: Dp = 140.dp,
    deadzone: Float = 0.1f,
    onValueChanged: (x: Float, y: Float) -> Unit,
    onPress: (() -> Unit)? = null,
) {
    var thumbOffset by remember { mutableStateOf(Offset.Zero) }
    var isDragging by remember { mutableStateOf(false) }

    Canvas(
        modifier = modifier
            .size(size)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        isDragging = true
                        val center = Offset(this.size.width / 2f, this.size.height / 2f)
                        val radius = this.size.width / 2f
                        val delta = offset - center
                        val dist = sqrt(delta.x * delta.x + delta.y * delta.y)
                        val clamped = if (dist > radius) delta * (radius / dist) else delta
                        thumbOffset = clamped
                        val nx = (clamped.x / radius).coerceIn(-1f, 1f)
                        val ny = -(clamped.y / radius).coerceIn(-1f, 1f) // Y inverted for gamepad convention
                        val appliedX = if (kotlin.math.abs(nx) < deadzone) 0f else nx
                        val appliedY = if (kotlin.math.abs(ny) < deadzone) 0f else ny
                        onValueChanged(appliedX, appliedY)
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        val center = Offset(this.size.width / 2f, this.size.height / 2f)
                        val radius = this.size.width / 2f
                        val delta = change.position - center
                        val dist = sqrt(delta.x * delta.x + delta.y * delta.y)
                        val clamped = if (dist > radius) delta * (radius / dist) else delta
                        thumbOffset = clamped
                        val nx = (clamped.x / radius).coerceIn(-1f, 1f)
                        val ny = -(clamped.y / radius).coerceIn(-1f, 1f)
                        val appliedX = if (kotlin.math.abs(nx) < deadzone) 0f else nx
                        val appliedY = if (kotlin.math.abs(ny) < deadzone) 0f else ny
                        onValueChanged(appliedX, appliedY)
                    },
                    onDragEnd = {
                        isDragging = false
                        thumbOffset = Offset.Zero
                        onValueChanged(0f, 0f)
                    },
                    onDragCancel = {
                        isDragging = false
                        thumbOffset = Offset.Zero
                        onValueChanged(0f, 0f)
                    },
                )
            }
    ) {
        val center = this.center
        val outerRadius = this.size.minDimension / 2f
        val thumbRadius = outerRadius * 0.35f

        // Outer ring
        drawCircle(
            color = DarkSurfaceVariant,
            radius = outerRadius,
            center = center,
        )
        drawCircle(
            color = ButtonBorder,
            radius = outerRadius,
            center = center,
            style = Stroke(width = 2f),
        )

        // Crosshair guides (subtle)
        val guideColor = ButtonBorder.copy(alpha = 0.3f)
        drawLine(guideColor, Offset(center.x, center.y - outerRadius * 0.6f), Offset(center.x, center.y + outerRadius * 0.6f), strokeWidth = 1f)
        drawLine(guideColor, Offset(center.x - outerRadius * 0.6f, center.y), Offset(center.x + outerRadius * 0.6f, center.y), strokeWidth = 1f)

        // Thumb
        val thumbCenter = center + thumbOffset
        val thumbColor = if (isDragging) AccentPurple else Color(0xFF5A5A66)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(thumbColor, thumbColor.copy(alpha = 0.7f)),
                center = thumbCenter,
                radius = thumbRadius,
            ),
            radius = thumbRadius,
            center = thumbCenter,
        )
        drawCircle(
            color = if (isDragging) AccentPurple.copy(alpha = 0.8f) else ButtonBorder,
            radius = thumbRadius,
            center = thumbCenter,
            style = Stroke(width = 2f),
        )

        // Inner circle detail on thumb
        drawCircle(
            color = if (isDragging) AccentPurple.copy(alpha = 0.4f) else ButtonBorder.copy(alpha = 0.3f),
            radius = thumbRadius * 0.4f,
            center = thumbCenter,
            style = Stroke(width = 1.5f),
        )
    }
}
