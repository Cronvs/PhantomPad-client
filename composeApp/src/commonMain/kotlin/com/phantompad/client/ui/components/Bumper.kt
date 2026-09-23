package com.phantompad.client.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.phantompad.client.ui.theme.*

/**
 * A bumper button (LB / RB).
 * Simple rectangular press zone at the top of the controller layout.
 */
@Composable
fun Bumper(
    label: String,
    modifier: Modifier = Modifier,
    width: Dp = 100.dp,
    height: Dp = 36.dp,
    onPressChanged: (Boolean) -> Unit,
) {
    var isPressed by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .width(width)
            .height(height)
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isPressed) AccentPurple.copy(alpha = 0.5f)
                else ButtonDefault
            )
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
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = if (isPressed) Color.White else DarkOnSurfaceVariant,
            letterSpacing = 1.sp,
        )
    }
}
