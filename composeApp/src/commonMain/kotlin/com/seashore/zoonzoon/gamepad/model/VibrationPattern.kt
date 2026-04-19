package com.seashore.zoonzoon.gamepad.model

/**
 * Represents a vibration pattern that can be executed on a gamepad controller.
 * 
 * Each pattern defines how motor values should be calculated over time.
 * Motor values are in the range [0.0, 1.0] where 0.0 is no vibration and 1.0 is maximum vibration.
 */
sealed class VibrationPattern {
    /**
     * Calculates the motor values for the current frame.
     * 
     * @param time The current time in milliseconds since pattern started
     * @param intensity The intensity multiplier in range [0.0, 1.0]
     * @return A pair of (leftMotor, rightMotor) values, each in range [0.0, 1.0]
     */
    abstract fun calculateFrame(time: Long, intensity: Float): Pair<Float, Float>
    
    /**
     * Constant vibration pattern - both motors vibrate at constant intensity.
     */
    data object Constant : VibrationPattern() {
        override fun calculateFrame(time: Long, intensity: Float): Pair<Float, Float> {
            return Pair(intensity, intensity)
        }
    }
    
    /**
     * Pulse vibration pattern - motors pulse on and off at a regular frequency.
     * 
     * @param frequencyHz The pulse frequency in Hertz (pulses per second)
     */
    data class Pulse(val frequencyHz: Float = 2.0f) : VibrationPattern() {
        override fun calculateFrame(time: Long, intensity: Float): Pair<Float, Float> {
            val period = 1000.0f / frequencyHz // Period in milliseconds
            val phase = (time % period) / period // Phase in [0.0, 1.0]
            val value = if (phase < 0.5f) intensity else 0.0f
            return Pair(value, value)
        }
    }
    
    /**
     * Wave vibration pattern - motors vibrate with smooth sine wave modulation.
     * 
     * @param frequencyHz The wave frequency in Hertz
     */
    data class Wave(val frequencyHz: Float = 1.0f) : VibrationPattern() {
        override fun calculateFrame(time: Long, intensity: Float): Pair<Float, Float> {
            val period = 1000.0f / frequencyHz // Period in milliseconds
            val phase = (time % period) / period // Phase in [0.0, 1.0]
            val angle = phase * 2.0f * kotlin.math.PI.toFloat()
            val sineValue = kotlin.math.sin(angle)
            // Map sine wave from [-1, 1] to [0, 1]
            val normalizedValue = (sineValue + 1.0f) / 2.0f
            val value = normalizedValue * intensity
            return Pair(value, value)
        }
    }
    
    /**
     * Custom vibration pattern - user-defined sequence of motor values.
     * 
     * @param frames List of frame definitions, each containing (leftMotor, rightMotor, durationMs)
     * @param loop Whether to loop the pattern when it reaches the end
     */
    data class Custom(
        val frames: List<Frame>,
        val loop: Boolean = true
    ) : VibrationPattern() {
        data class Frame(
            val leftMotor: Float,
            val rightMotor: Float,
            val durationMs: Long
        )
        
        override fun calculateFrame(time: Long, intensity: Float): Pair<Float, Float> {
            if (frames.isEmpty()) {
                return Pair(0.0f, 0.0f)
            }
            
            var accumulatedTime = 0L
            var currentTime = time
            
            // If looping, wrap time to pattern duration
            if (loop) {
                val totalDuration = frames.sumOf { it.durationMs }
                if (totalDuration > 0) {
                    currentTime = time % totalDuration
                }
            }
            
            // Find the current frame
            for (frame in frames) {
                if (currentTime < accumulatedTime + frame.durationMs) {
                    return Pair(
                        frame.leftMotor * intensity,
                        frame.rightMotor * intensity
                    )
                }
                accumulatedTime += frame.durationMs
            }
            
            // If we've gone past all frames and not looping, return zero
            return Pair(0.0f, 0.0f)
        }
    }
}
