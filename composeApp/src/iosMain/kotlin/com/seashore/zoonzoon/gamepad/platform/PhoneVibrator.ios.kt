package com.seashore.zoonzoon.gamepad.platform

import GameControllerHaptics.GameControllerHaptics
import kotlinx.cinterop.ExperimentalForeignApi

actual fun createPhoneVibrator(): PhoneVibrator = IosPhoneVibrator()

/**
 * Phone rumble via continuous Core Haptics (ObjC bridge).
 *
 * The old UIImpactFeedbackGenerator path only fired discrete taps and got
 * rate-limited whenever gamepad Core Haptics was also running — which made
 * Gamepad+Phone feel like rare knocks while Phone-only felt denser.
 */
@OptIn(ExperimentalForeignApi::class)
private class IosPhoneVibrator : PhoneVibrator {
    // Dedicated bridge instance so gamepad stopAll() never kills phone rumble.
    private val haptics = GameControllerHaptics()
    private var prepared = false

    override val isAvailable: Boolean = true

    override fun vibrate(intensity: Float) {
        if (!prepared) {
            prepared = haptics.preparePhoneEngine()
            if (!prepared) return
        }
        haptics.updatePhoneIntensity(intensity)
    }

    override fun stop() {
        haptics.stopPhone()
        prepared = false
    }
}
