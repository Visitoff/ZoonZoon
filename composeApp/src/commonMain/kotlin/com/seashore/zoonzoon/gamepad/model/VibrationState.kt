package com.seashore.zoonzoon.gamepad.model

/**
 * Represents the current state of vibration execution.
 * 
 * This data class tracks whether vibration is enabled, which pattern is active,
 * and the current intensity level.
 * 
 * @param enabled Whether vibration is currently enabled
 * @param activePattern The currently selected vibration pattern
 * @param intensity The current intensity level in range [0.0, 1.0]
 */
data class VibrationState(
    val enabled: Boolean = false,
    val activePattern: VibrationPattern = VibrationPattern.Constant,
    val intensity: Float = 0.7f,
    val sharpness: Float = 0.5f
) {
    init {
        require(intensity in 0.0f..1.0f) {
            "Intensity must be in range [0.0, 1.0], got $intensity"
        }
        require(sharpness in 0.0f..1.0f) {
            "Sharpness must be in range [0.0, 1.0], got $sharpness"
        }
    }
}
