package com.seashore.zoonzoon.gamepad.platform

/**
 * Drives the phone's built-in haptic / vibration motor.
 */
interface PhoneVibrator {
    val isAvailable: Boolean
    /** @param sharpness Core Haptics character [0,1]; ignored on Android. */
    fun vibrate(intensity: Float, sharpness: Float = 0.5f)
    fun stop()
}

object NoOpPhoneVibrator : PhoneVibrator {
    override val isAvailable: Boolean = false
    override fun vibrate(intensity: Float, sharpness: Float) = Unit
    override fun stop() = Unit
}

expect fun createPhoneVibrator(): PhoneVibrator
