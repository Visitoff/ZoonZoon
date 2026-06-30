package com.seashore.zoonzoon.gamepad.platform

/**
 * Drives the phone's built-in haptic / vibration motor.
 */
interface PhoneVibrator {
    val isAvailable: Boolean
    fun vibrate(intensity: Float)
    fun stop()
}

object NoOpPhoneVibrator : PhoneVibrator {
    override val isAvailable: Boolean = false
    override fun vibrate(intensity: Float) = Unit
    override fun stop() = Unit
}

expect fun createPhoneVibrator(): PhoneVibrator
