package com.phantompad.client.network

/**
 * Platform-specific haptic feedback (vibration).
 * Android: uses Vibrator/VibrationEffect
 * iOS: uses UIImpactFeedbackGenerator / CoreHaptics
 */
expect class HapticManager {
    /**
     * Trigger haptic feedback with the given motor magnitudes.
     * @param strongMagnitude Large motor intensity (0-65535)
     * @param weakMagnitude Small motor intensity (0-65535)
     */
    fun vibrate(strongMagnitude: Int, weakMagnitude: Int)
}
