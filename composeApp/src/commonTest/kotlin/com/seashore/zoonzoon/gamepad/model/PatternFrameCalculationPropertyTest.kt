package com.seashore.zoonzoon.gamepad.model

import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Property-based tests for pattern frame calculation.
 * 
 * **Validates: Requirements 5.1, 5.2**
 * 
 * Property 11: Pattern Frame Calculation and Command Sending
 * For any active vibration pattern, the VibrationEngine SHALL calculate frame values 
 * for left and right motors and send vibration commands to the PlatformGamepadController.
 * 
 * This test validates that all pattern types generate valid motor values in the range [0.0, 1.0].
 */
class PatternFrameCalculationPropertyTest {
    
    /**
     * Property: All patterns must generate motor values in valid range [0.0, 1.0]
     * 
     * Tests all pattern types (Constant, Pulse, Wave, Custom) with various:
     * - Time values (0ms to 10000ms)
     * - Intensity values (0.0 to 1.0)
     * - Pattern-specific parameters (frequencies, custom frames)
     */
    @Test
    fun testAllPatternsGenerateValidMotorValues() {
        val timeValues = listOf(0L, 1L, 10L, 50L, 100L, 250L, 500L, 1000L, 2000L, 5000L, 10000L)
        val intensityValues = listOf(0.0f, 0.1f, 0.25f, 0.5f, 0.75f, 0.9f, 1.0f)
        
        // Test Constant pattern
        val constantPattern = VibrationPattern.Constant
        for (time in timeValues) {
            for (intensity in intensityValues) {
                val (left, right) = constantPattern.calculateFrame(time, intensity)
                assertMotorValuesInValidRange(left, right, "Constant", time, intensity)
            }
        }
        
        // Test Pulse pattern with various frequencies
        val pulseFrequencies = listOf(0.5f, 1.0f, 2.0f, 5.0f, 10.0f)
        for (frequency in pulseFrequencies) {
            val pulsePattern = VibrationPattern.Pulse(frequency)
            for (time in timeValues) {
                for (intensity in intensityValues) {
                    val (left, right) = pulsePattern.calculateFrame(time, intensity)
                    assertMotorValuesInValidRange(left, right, "Pulse(${frequency}Hz)", time, intensity)
                }
            }
        }
        
        // Test Wave pattern with various frequencies
        val waveFrequencies = listOf(0.25f, 0.5f, 1.0f, 2.0f, 5.0f, 10.0f)
        for (frequency in waveFrequencies) {
            val wavePattern = VibrationPattern.Wave(frequency)
            for (time in timeValues) {
                for (intensity in intensityValues) {
                    val (left, right) = wavePattern.calculateFrame(time, intensity)
                    assertMotorValuesInValidRange(left, right, "Wave(${frequency}Hz)", time, intensity)
                }
            }
        }
        
        // Test Custom pattern with various frame configurations
        val customPatternConfigs = listOf(
            // Single frame
            listOf(VibrationPattern.Custom.Frame(0.5f, 0.5f, 100L)),
            // Multiple frames with different values
            listOf(
                VibrationPattern.Custom.Frame(0.0f, 0.0f, 100L),
                VibrationPattern.Custom.Frame(0.5f, 0.5f, 100L),
                VibrationPattern.Custom.Frame(1.0f, 1.0f, 100L)
            ),
            // Asymmetric motor values
            listOf(
                VibrationPattern.Custom.Frame(0.3f, 0.7f, 150L),
                VibrationPattern.Custom.Frame(0.8f, 0.2f, 150L)
            ),
            // Empty frames (edge case)
            emptyList()
        )
        
        for ((index, frames) in customPatternConfigs.withIndex()) {
            val customPattern = VibrationPattern.Custom(frames, loop = true)
            for (time in timeValues) {
                for (intensity in intensityValues) {
                    val (left, right) = customPattern.calculateFrame(time, intensity)
                    assertMotorValuesInValidRange(left, right, "Custom(config$index)", time, intensity)
                }
            }
            
            // Also test non-looping variant
            val customPatternNoLoop = VibrationPattern.Custom(frames, loop = false)
            for (time in timeValues) {
                for (intensity in intensityValues) {
                    val (left, right) = customPatternNoLoop.calculateFrame(time, intensity)
                    assertMotorValuesInValidRange(left, right, "Custom(config$index,noloop)", time, intensity)
                }
            }
        }
    }
    
    /**
     * Property: Motor values must remain valid across extended time periods
     * 
     * Tests that patterns maintain valid motor values over long durations,
     * ensuring no overflow or numerical instability issues.
     */
    @Test
    fun testMotorValuesValidAcrossExtendedTime() {
        val patterns = listOf(
            VibrationPattern.Constant,
            VibrationPattern.Pulse(2.0f),
            VibrationPattern.Wave(1.0f),
            VibrationPattern.Custom(
                listOf(
                    VibrationPattern.Custom.Frame(0.5f, 0.5f, 100L),
                    VibrationPattern.Custom.Frame(0.8f, 0.2f, 100L)
                ),
                loop = true
            )
        )
        
        // Test over very long time periods (up to 1 hour)
        val extendedTimeValues = listOf(
            0L,
            60_000L,      // 1 minute
            600_000L,     // 10 minutes
            3_600_000L,   // 1 hour
            Long.MAX_VALUE / 2  // Very large time value
        )
        
        val intensities = listOf(0.0f, 0.5f, 1.0f)
        
        for (pattern in patterns) {
            for (time in extendedTimeValues) {
                for (intensity in intensities) {
                    val (left, right) = pattern.calculateFrame(time, intensity)
                    assertMotorValuesInValidRange(
                        left, right, 
                        pattern::class.simpleName ?: "Unknown", 
                        time, intensity
                    )
                }
            }
        }
    }
    
    /**
     * Property: Motor values must be valid for all edge case intensities
     * 
     * Tests boundary conditions and edge cases for intensity values.
     */
    @Test
    fun testMotorValuesValidForEdgeCaseIntensities() {
        val patterns = listOf(
            VibrationPattern.Constant,
            VibrationPattern.Pulse(2.0f),
            VibrationPattern.Wave(1.0f),
            VibrationPattern.Custom(
                listOf(VibrationPattern.Custom.Frame(0.5f, 0.5f, 100L)),
                loop = true
            )
        )
        
        // Edge case intensity values
        val edgeCaseIntensities = listOf(
            0.0f,                    // Minimum
            Float.MIN_VALUE,         // Smallest positive value
            0.0001f,                 // Very small
            0.9999f,                 // Very close to max
            1.0f,                    // Maximum
            0.5f,                    // Middle
            0.3333333f,              // Repeating decimal
            0.6666666f               // Repeating decimal
        )
        
        val times = listOf(0L, 100L, 500L, 1000L)
        
        for (pattern in patterns) {
            for (time in times) {
                for (intensity in edgeCaseIntensities) {
                    val (left, right) = pattern.calculateFrame(time, intensity)
                    assertMotorValuesInValidRange(
                        left, right,
                        pattern::class.simpleName ?: "Unknown",
                        time, intensity
                    )
                }
            }
        }
    }
    
    /**
     * Property: Custom patterns with extreme frame values must still produce valid output
     * 
     * Tests that custom patterns with boundary motor values (0.0 and 1.0) 
     * combined with various intensities always produce valid results.
     */
    @Test
    fun testCustomPatternsWithExtremeFrameValues() {
        val extremeFrameConfigs = listOf(
            // All zeros
            listOf(
                VibrationPattern.Custom.Frame(0.0f, 0.0f, 100L),
                VibrationPattern.Custom.Frame(0.0f, 0.0f, 100L)
            ),
            // All max
            listOf(
                VibrationPattern.Custom.Frame(1.0f, 1.0f, 100L),
                VibrationPattern.Custom.Frame(1.0f, 1.0f, 100L)
            ),
            // Mixed extremes
            listOf(
                VibrationPattern.Custom.Frame(0.0f, 1.0f, 100L),
                VibrationPattern.Custom.Frame(1.0f, 0.0f, 100L)
            ),
            // Rapid transitions
            listOf(
                VibrationPattern.Custom.Frame(0.0f, 0.0f, 1L),
                VibrationPattern.Custom.Frame(1.0f, 1.0f, 1L),
                VibrationPattern.Custom.Frame(0.0f, 0.0f, 1L),
                VibrationPattern.Custom.Frame(1.0f, 1.0f, 1L)
            )
        )
        
        val times = listOf(0L, 1L, 50L, 100L, 150L, 200L, 500L)
        val intensities = listOf(0.0f, 0.25f, 0.5f, 0.75f, 1.0f)
        
        for ((index, frames) in extremeFrameConfigs.withIndex()) {
            val pattern = VibrationPattern.Custom(frames, loop = true)
            for (time in times) {
                for (intensity in intensities) {
                    val (left, right) = pattern.calculateFrame(time, intensity)
                    assertMotorValuesInValidRange(
                        left, right,
                        "CustomExtreme(config$index)",
                        time, intensity
                    )
                }
            }
        }
    }
    
    /**
     * Property: Pattern frame calculation must be deterministic
     * 
     * Tests that calling calculateFrame with the same parameters always 
     * produces the same result (no randomness or state mutation).
     */
    @Test
    fun testPatternFrameCalculationIsDeterministic() {
        val patterns = listOf(
            VibrationPattern.Constant,
            VibrationPattern.Pulse(2.0f),
            VibrationPattern.Wave(1.0f),
            VibrationPattern.Custom(
                listOf(
                    VibrationPattern.Custom.Frame(0.3f, 0.7f, 100L),
                    VibrationPattern.Custom.Frame(0.8f, 0.2f, 100L)
                ),
                loop = true
            )
        )
        
        val times = listOf(0L, 100L, 500L, 1000L)
        val intensities = listOf(0.0f, 0.5f, 1.0f)
        
        for (pattern in patterns) {
            for (time in times) {
                for (intensity in intensities) {
                    // Calculate the same frame multiple times
                    val result1 = pattern.calculateFrame(time, intensity)
                    val result2 = pattern.calculateFrame(time, intensity)
                    val result3 = pattern.calculateFrame(time, intensity)
                    
                    // All results must be identical
                    assertTrue(
                        result1 == result2 && result2 == result3,
                        "Pattern ${pattern::class.simpleName} must produce deterministic results. " +
                        "Got different values for time=$time, intensity=$intensity: " +
                        "result1=$result1, result2=$result2, result3=$result3"
                    )
                    
                    // And all must be in valid range
                    assertMotorValuesInValidRange(
                        result1.first, result1.second,
                        pattern::class.simpleName ?: "Unknown",
                        time, intensity
                    )
                }
            }
        }
    }
    
    /**
     * Helper function to assert that motor values are in the valid range [0.0, 1.0]
     */
    private fun assertMotorValuesInValidRange(
        leftMotor: Float,
        rightMotor: Float,
        patternName: String,
        time: Long,
        intensity: Float
    ) {
        assertTrue(
            leftMotor >= 0.0f && leftMotor <= 1.0f,
            "Left motor value $leftMotor is out of valid range [0.0, 1.0] for pattern $patternName " +
            "at time=$time, intensity=$intensity"
        )
        assertTrue(
            rightMotor >= 0.0f && rightMotor <= 1.0f,
            "Right motor value $rightMotor is out of valid range [0.0, 1.0] for pattern $patternName " +
            "at time=$time, intensity=$intensity"
        )
        
        // Also check for NaN and Infinity
        assertTrue(
            !leftMotor.isNaN() && !leftMotor.isInfinite(),
            "Left motor value $leftMotor is NaN or Infinite for pattern $patternName " +
            "at time=$time, intensity=$intensity"
        )
        assertTrue(
            !rightMotor.isNaN() && !rightMotor.isInfinite(),
            "Right motor value $rightMotor is NaN or Infinite for pattern $patternName " +
            "at time=$time, intensity=$intensity"
        )
    }
}
