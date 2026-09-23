package com.phantompad.client.network

import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIImpactFeedbackStyle

actual class HapticManager {

    private val heavyGenerator = UIImpactFeedbackGenerator(style = UIImpactFeedbackStyle.UIImpactFeedbackStyleHeavy)
    private val mediumGenerator = UIImpactFeedbackGenerator(style = UIImpactFeedbackStyle.UIImpactFeedbackStyleMedium)
    private val lightGenerator = UIImpactFeedbackGenerator(style = UIImpactFeedbackStyle.UIImpactFeedbackStyleLight)

    actual fun vibrate(strongMagnitude: Int, weakMagnitude: Int) {
        val total = strongMagnitude + weakMagnitude
        if (total <= 0) return

        val intensity = (total.toDouble() / 131070.0).coerceIn(0.0, 1.0)

        when {
            intensity > 0.7 -> {
                heavyGenerator.prepare()
                heavyGenerator.impactOccurredWithIntensity(intensity)
            }
            intensity > 0.3 -> {
                mediumGenerator.prepare()
                mediumGenerator.impactOccurredWithIntensity(intensity)
            }
            else -> {
                lightGenerator.prepare()
                lightGenerator.impactOccurredWithIntensity(intensity)
            }
        }
    }
}
