package com.seashore.zoonzoon.gamepad.platform

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

private lateinit var vibratorContext: Context

internal fun initPhoneVibratorContext(context: Context) {
    vibratorContext = context.applicationContext
}

actual fun createPhoneVibrator(): PhoneVibrator = AndroidPhoneVibrator()

private class AndroidPhoneVibrator : PhoneVibrator {
    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = vibratorContext.getSystemService(VibratorManager::class.java)
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            vibratorContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    override val isAvailable: Boolean
        get() = vibrator?.hasVibrator() == true

    override fun vibrate(intensity: Float, sharpness: Float) {
        val v = vibrator ?: return
        if (!v.hasVibrator()) return
        val clamped = intensity.coerceIn(0f, 1f)
        if (clamped <= 0.01f) {
            stop()
            return
        }
        val amplitude = (255f * clamped).toInt().coerceIn(1, 255)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(50L, amplitude))
        } else {
            @Suppress("DEPRECATION")
            v.vibrate(50L)
        }
    }

    override fun stop() {
        vibrator?.cancel()
    }
}
