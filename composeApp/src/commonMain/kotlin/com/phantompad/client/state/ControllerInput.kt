package com.phantompad.client.state

import com.phantompad.client.protocol.ControllerButtons
import com.phantompad.client.protocol.ControllerState

/**
 * Mutable controller input state that the UI writes into.
 * Converts to the binary ControllerState for network transmission.
 *
 * Analog stick values are normalized -1.0..1.0 by the UI components
 * and converted to the int16 range (-32768..32767) here.
 * Trigger values are 0.0..1.0, converted to 0..255.
 */
class ControllerInput {
    // Buttons - each can be independently pressed
    var a: Boolean = false
    var b: Boolean = false
    var x: Boolean = false
    var y: Boolean = false
    var leftBumper: Boolean = false
    var rightBumper: Boolean = false
    var back: Boolean = false
    var start: Boolean = false
    var guide: Boolean = false
    var leftThumbButton: Boolean = false
    var rightThumbButton: Boolean = false
    var dpadUp: Boolean = false
    var dpadDown: Boolean = false
    var dpadLeft: Boolean = false
    var dpadRight: Boolean = false

    // Analog sticks (-1.0 to 1.0)
    var leftStickX: Float = 0f
    var leftStickY: Float = 0f
    var rightStickX: Float = 0f
    var rightStickY: Float = 0f

    // Triggers (0.0 to 1.0)
    var leftTrigger: Float = 0f
    var rightTrigger: Float = 0f

    // Gyro (raw int16 values, optional)
    var gyroX: Short = 0
    var gyroY: Short = 0
    var gyroZ: Short = 0

    /**
     * Convert to the packed binary ControllerState for the protocol.
     */
    fun toControllerState(): ControllerState {
        var buttons: UShort = 0u
        if (a) buttons = buttons or ControllerButtons.A
        if (b) buttons = buttons or ControllerButtons.B
        if (x) buttons = buttons or ControllerButtons.X
        if (y) buttons = buttons or ControllerButtons.Y
        if (leftBumper) buttons = buttons or ControllerButtons.LEFT_BUMPER
        if (rightBumper) buttons = buttons or ControllerButtons.RIGHT_BUMPER
        if (back) buttons = buttons or ControllerButtons.BACK
        if (start) buttons = buttons or ControllerButtons.START
        if (guide) buttons = buttons or ControllerButtons.GUIDE
        if (leftThumbButton) buttons = buttons or ControllerButtons.LEFT_THUMB
        if (rightThumbButton) buttons = buttons or ControllerButtons.RIGHT_THUMB
        if (dpadUp) buttons = buttons or ControllerButtons.DPAD_UP
        if (dpadDown) buttons = buttons or ControllerButtons.DPAD_DOWN
        if (dpadLeft) buttons = buttons or ControllerButtons.DPAD_LEFT
        if (dpadRight) buttons = buttons or ControllerButtons.DPAD_RIGHT

        return ControllerState(
            buttons = buttons,
            leftThumbX = floatToAxis(leftStickX),
            leftThumbY = floatToAxis(leftStickY),
            rightThumbX = floatToAxis(rightStickX),
            rightThumbY = floatToAxis(rightStickY),
            gyroX = gyroX,
            gyroY = gyroY,
            gyroZ = gyroZ,
            leftTrigger = floatToTrigger(leftTrigger),
            rightTrigger = floatToTrigger(rightTrigger),
        )
    }

    /**
     * Reset all inputs to neutral positions.
     */
    fun reset() {
        a = false; b = false; x = false; y = false
        leftBumper = false; rightBumper = false
        back = false; start = false; guide = false
        leftThumbButton = false; rightThumbButton = false
        dpadUp = false; dpadDown = false; dpadLeft = false; dpadRight = false
        leftStickX = 0f; leftStickY = 0f
        rightStickX = 0f; rightStickY = 0f
        leftTrigger = 0f; rightTrigger = 0f
        gyroX = 0; gyroY = 0; gyroZ = 0
    }

    companion object {
        /** Convert -1.0..1.0 float to -32768..32767 int16 */
        fun floatToAxis(value: Float): Short {
            val clamped = value.coerceIn(-1f, 1f)
            return if (clamped >= 0) {
                (clamped * 32767f).toInt().toShort()
            } else {
                (clamped * 32768f).toInt().toShort()
            }
        }

        /** Convert 0.0..1.0 float to 0..255 uint8 */
        fun floatToTrigger(value: Float): UByte {
            return (value.coerceIn(0f, 1f) * 255f).toInt().toUByte()
        }
    }
}
