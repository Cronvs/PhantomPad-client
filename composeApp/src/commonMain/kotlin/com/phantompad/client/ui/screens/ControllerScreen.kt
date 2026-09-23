package com.phantompad.client.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.phantompad.client.state.ControllerInput
import com.phantompad.client.ui.components.*
import com.phantompad.client.ui.theme.*

/**
 * Full-screen landscape controller layout.
 *
 * Layout structure:
 * ┌──────────────────────────────────────────────────────────────┐
 * │  [LT]  [LB]           [Back][Guide][Start]       [RB]  [RT] │
 * │                                                              │
 * │  ┌─────┐                                       [Y]          │
 * │  │ D-  │     (Left                          [X]   [B]       │
 * │  │ Pad │      Stick)      Status            [A]             │
 * │  └─────┘                                                     │
 * │           (Left                                  (Right      │
 * │            Stick)           [P1]  [3ms]           Stick)     │
 * └──────────────────────────────────────────────────────────────┘
 */
@Composable
fun ControllerScreen(
    serverName: String,
    slotId: Int,
    latencyMs: Long,
    controllerInput: ControllerInput,
    onInputChanged: () -> Unit,
    onDisconnect: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Main layout
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // === LEFT SIDE === (Trigger, Bumper, D-Pad, Left Stick)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly,
            ) {
                // Top row: LT + LB
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Trigger(
                        label = "LT",
                        width = 38.dp,
                        height = 64.dp,
                        onValueChanged = { value ->
                            controllerInput.leftTrigger = value
                            onInputChanged()
                        },
                    )
                    Bumper(
                        label = "LB",
                        width = 80.dp,
                        height = 32.dp,
                        onPressChanged = { pressed ->
                            controllerInput.leftBumper = pressed
                            onInputChanged()
                        },
                    )
                }

                // D-Pad
                DPad(
                    size = 110.dp,
                    onDirectionChanged = { up, down, left, right ->
                        controllerInput.dpadUp = up
                        controllerInput.dpadDown = down
                        controllerInput.dpadLeft = left
                        controllerInput.dpadRight = right
                        onInputChanged()
                    },
                )

                // Left analog stick
                AnalogStick(
                    size = 120.dp,
                    onValueChanged = { x, y ->
                        controllerInput.leftStickX = x
                        controllerInput.leftStickY = y
                        onInputChanged()
                    },
                    onPress = {
                        controllerInput.leftThumbButton = !controllerInput.leftThumbButton
                        onInputChanged()
                    },
                )
            }

            // === CENTER === (Menu buttons + status)
            Column(
                modifier = Modifier
                    .widthIn(min = 120.dp)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                // Menu buttons
                MenuButtons(
                    onBackChanged = { pressed ->
                        controllerInput.back = pressed
                        onInputChanged()
                    },
                    onGuideChanged = { pressed ->
                        controllerInput.guide = pressed
                        onInputChanged()
                    },
                    onStartChanged = { pressed ->
                        controllerInput.start = pressed
                        onInputChanged()
                    },
                )

                Spacer(Modifier.height(16.dp))

                // Status info
                Text(
                    text = serverName,
                    fontSize = 11.sp,
                    color = DarkOnSurfaceVariant,
                    maxLines = 1,
                )

                Spacer(Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Player slot
                    StatusBadge(
                        text = "P${slotId + 1}",
                        color = AccentPurple,
                    )

                    // Latency
                    StatusBadge(
                        text = "${latencyMs}ms",
                        color = when {
                            latencyMs < 10 -> StatusConnected
                            latencyMs < 50 -> Color(0xFFFBBC04)
                            else -> StatusError
                        },
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Disconnect button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(StatusError.copy(alpha = 0.15f))
                        .clickable(onClick = onDisconnect)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Disconnect",
                        fontSize = 10.sp,
                        color = StatusError,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }

            // === RIGHT SIDE === (Trigger, Bumper, Face Buttons, Right Stick)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly,
            ) {
                // Top row: RB + RT
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Bumper(
                        label = "RB",
                        width = 80.dp,
                        height = 32.dp,
                        onPressChanged = { pressed ->
                            controllerInput.rightBumper = pressed
                            onInputChanged()
                        },
                    )
                    Trigger(
                        label = "RT",
                        width = 38.dp,
                        height = 64.dp,
                        onValueChanged = { value ->
                            controllerInput.rightTrigger = value
                            onInputChanged()
                        },
                    )
                }

                // Face buttons (A/B/X/Y)
                FaceButtons(
                    size = 130.dp,
                    onButtonChanged = { a, b, x, y ->
                        controllerInput.a = a
                        controllerInput.b = b
                        controllerInput.x = x
                        controllerInput.y = y
                        onInputChanged()
                    },
                )

                // Right analog stick
                AnalogStick(
                    size = 120.dp,
                    onValueChanged = { x, y ->
                        controllerInput.rightStickX = x
                        controllerInput.rightStickY = y
                        onInputChanged()
                    },
                    onPress = {
                        controllerInput.rightThumbButton = !controllerInput.rightThumbButton
                        onInputChanged()
                    },
                )
            }
        }
    }
}

@Composable
private fun StatusBadge(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = color,
        )
    }
}
