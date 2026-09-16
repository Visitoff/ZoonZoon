package com.seashore.zoonzoon.gamepad.engine

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** Peak motor output while dragging the Home waveform, matching the iOS revival client. */
const val TouchHapticMaxScale = 0.68f

/**
 * Maps a vertical finger position on the waveform to left/right motors.
 *
 * Top (y = 0) drives the left handle; bottom (y = 1) drives the right.
 * Output is scaled by [intensity] and [TouchHapticMaxScale].
 */
fun touchHapticMotors(normalizedY: Float, intensity: Float): Pair<Float, Float> {
    val y = normalizedY.coerceIn(0f, 1f)
    val maximumOutput = intensity.coerceIn(0f, 1f) * TouchHapticMaxScale
    val left = cos(y * PI / 2.0).toFloat() * maximumOutput
    val right = sin(y * PI / 2.0).toFloat() * maximumOutput
    return left to right
}
