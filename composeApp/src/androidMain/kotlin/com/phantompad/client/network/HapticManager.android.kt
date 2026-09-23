package com.phantompad.client.network

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

actual class HapticManager(private val context: Context) {

    private val vibrator: Vibrator by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vm.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    actual fun vibrate(strongMagnitude: Int, weakMagnitude: Int) {
        // Map 0-65535 range to 0-255 amplitude and reasonable duration
        val amplitude = ((strongMagnitude + weakMagnitude) / 2)
            .coerceIn(0, 65535)
            .let { it * 255 / 65535 }
            .coerceIn(1, 255)

        val durationMs = if (strongMagnitude > 0 || weakMagnitude > 0) {
            // Scale duration based on magnitude (50ms - 200ms)
            val maxMag = maxOf(strongMagnitude, weakMagnitude)
            (50L + (maxMag.toLong() * 150 / 65535)).coerceIn(50, 200)
        } else {
            return // no vibration needed
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createOneShot(durationMs, amplitude)
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(durationMs)
        }
    }
}
