package com.seashore.zoonzoon.gamepad.platform

import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIImpactFeedbackStyle

actual fun createPhoneVibrator(): PhoneVibrator = IosPhoneVibrator()

private class IosPhoneVibrator : PhoneVibrator {
    private val generator = UIImpactFeedbackGenerator(UIImpactFeedbackStyle.UIImpactFeedbackStyleHeavy)

    override val isAvailable: Boolean = true

    override fun vibrate(intensity: Float) {
        if (intensity <= 0.01f) return
        generator.prepare()
        generator.impactOccurred()
    }

    override fun stop() = Unit
}
