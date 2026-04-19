package com.seashore.zoonzoon.gamepad.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for VibrationPattern implementations.
 * 
 * Tests each pattern type's calculateFrame method with various inputs.
 */
class VibrationPatternTest {
    
    // ConstantPattern Tests
    
    @Test
    fun testConstantPatternWithZeroIntensity() {
        val pattern = VibrationPattern.Constant
        val (left, right) = pattern.calculateFrame(0, 0.0f)
        
        assertEquals(0.0f, left, "Left motor should be 0.0 with zero intensity")
        assertEquals(0.0f, right, "Right motor should be 0.0 with zero intensity")
    }
    
    @Test
    fun testConstantPatternWithMaxIntensity() {
        val pattern = VibrationPattern.Constant
        val (left, right) = pattern.calculateFrame(0, 1.0f)
        
        assertEquals(1.0f, left, "Left motor should be 1.0 with max intensity")
        assertEquals(1.0f, right, "Right motor should be 1.0 with max intensity")
    }
    
    @Test
    fun testConstantPatternWithMidRangeIntensity() {
        val pattern = VibrationPattern.Constant
        val (left, right) = pattern.calculateFrame(0, 0.5f)
        
        assertEquals(0.5f, left, "Left motor should be 0.5 with mid-range intensity")
        assertEquals(0.5f, right, "Right motor should be 0.5 with mid-range intensity")
    }
    
    @Test
    fun testConstantPatternWithVariousIntensities() {
        val pattern = VibrationPattern.Constant
        val intensities = listOf(0.0f, 0.25f, 0.5f, 0.75f, 1.0f)
        
        for (intensity in intensities) {
            val (left, right) = pattern.calculateFrame(0, intensity)
            assertEquals(intensity, left, "Left motor should match intensity $intensity")
            assertEquals(intensity, right, "Right motor should match intensity $intensity")
        }
    }
    
    @Test
    fun testConstantPatternTimeIndependence() {
        val pattern = VibrationPattern.Constant
        val intensity = 0.7f
        val times = listOf(0L, 100L, 1000L, 10000L)
        
        for (time in times) {
            val (left, right) = pattern.calculateFrame(time, intensity)
            assertEquals(intensity, left, "Left motor should be constant at time $time")
            assertEquals(intensity, right, "Right motor should be constant at time $time")
        }
    }
    
    @Test
    fun testConstantPatternMotorValuesInValidRange() {
        val pattern = VibrationPattern.Constant
        val intensities = listOf(0.0f, 0.1f, 0.3f, 0.5f, 0.7f, 0.9f, 1.0f)
        
        for (intensity in intensities) {
            val (left, right) = pattern.calculateFrame(0, intensity)
            assertTrue(left >= 0.0f && left <= 1.0f, "Left motor value $left should be in range [0.0, 1.0]")
            assertTrue(right >= 0.0f && right <= 1.0f, "Right motor value $right should be in range [0.0, 1.0]")
        }
    }
    
    @Test
    fun testConstantPatternBothMotorsEqual() {
        val pattern = VibrationPattern.Constant
        val intensities = listOf(0.0f, 0.3f, 0.6f, 1.0f)
        
        for (intensity in intensities) {
            val (left, right) = pattern.calculateFrame(0, intensity)
            assertEquals(left, right, "Both motors should have equal values for constant pattern")
        }
    }
    
    // PulsePattern Tests
    
    @Test
    fun testPulsePatternWithZeroIntensity() {
        val pattern = VibrationPattern.Pulse(frequencyHz = 2.0f)
        val (left, right) = pattern.calculateFrame(0, 0.0f)
        
        assertEquals(0.0f, left, "Left motor should be 0.0 with zero intensity")
        assertEquals(0.0f, right, "Right motor should be 0.0 with zero intensity")
    }
    
    @Test
    fun testPulsePatternWithMaxIntensity() {
        val pattern = VibrationPattern.Pulse(frequencyHz = 2.0f)
        // At time 0, phase is 0.0, which is < 0.5, so should be at max intensity
        val (left, right) = pattern.calculateFrame(0, 1.0f)
        
        assertEquals(1.0f, left, "Left motor should be 1.0 at start of pulse")
        assertEquals(1.0f, right, "Right motor should be 1.0 at start of pulse")
    }
    
    @Test
    fun testPulsePatternSquareWaveBehavior() {
        val pattern = VibrationPattern.Pulse(frequencyHz = 2.0f) // 2 Hz = 500ms period
        val intensity = 0.8f
        
        // First half of period (0-250ms): should be ON (intensity)
        val (left1, right1) = pattern.calculateFrame(0, intensity)
        assertEquals(intensity, left1, "Should be ON at start of period")
        assertEquals(intensity, right1, "Should be ON at start of period")
        
        val (left2, right2) = pattern.calculateFrame(100, intensity)
        assertEquals(intensity, left2, "Should be ON at 100ms (first half)")
        assertEquals(intensity, right2, "Should be ON at 100ms (first half)")
        
        val (left3, right3) = pattern.calculateFrame(249, intensity)
        assertEquals(intensity, left3, "Should be ON at 249ms (just before halfway)")
        assertEquals(intensity, right3, "Should be ON at 249ms (just before halfway)")
        
        // Second half of period (250-500ms): should be OFF (0.0)
        val (left4, right4) = pattern.calculateFrame(250, intensity)
        assertEquals(0.0f, left4, "Should be OFF at 250ms (second half)")
        assertEquals(0.0f, right4, "Should be OFF at 250ms (second half)")
        
        val (left5, right5) = pattern.calculateFrame(400, intensity)
        assertEquals(0.0f, left5, "Should be OFF at 400ms (second half)")
        assertEquals(0.0f, right5, "Should be OFF at 400ms (second half)")
        
        val (left6, right6) = pattern.calculateFrame(499, intensity)
        assertEquals(0.0f, left6, "Should be OFF at 499ms (end of period)")
        assertEquals(0.0f, right6, "Should be OFF at 499ms (end of period)")
    }
    
    @Test
    fun testPulsePatternPeriodicity() {
        val pattern = VibrationPattern.Pulse(frequencyHz = 2.0f) // 500ms period
        val intensity = 0.7f
        
        // Test that pattern repeats after one period
        val (left1, right1) = pattern.calculateFrame(0, intensity)
        val (left2, right2) = pattern.calculateFrame(500, intensity)
        
        assertEquals(left1, left2, "Pattern should repeat after one period")
        assertEquals(right1, right2, "Pattern should repeat after one period")
        
        // Test at quarter period
        val (left3, right3) = pattern.calculateFrame(125, intensity)
        val (left4, right4) = pattern.calculateFrame(625, intensity)
        
        assertEquals(left3, left4, "Pattern should repeat at quarter period offset")
        assertEquals(right3, right4, "Pattern should repeat at quarter period offset")
    }
    
    @Test
    fun testPulsePatternDifferentFrequencies() {
        val intensity = 0.6f
        
        // Test 1 Hz (1000ms period)
        val pattern1Hz = VibrationPattern.Pulse(frequencyHz = 1.0f)
        val (left1, right1) = pattern1Hz.calculateFrame(0, intensity)
        assertEquals(intensity, left1, "1Hz pattern should be ON at start")
        
        val (left2, right2) = pattern1Hz.calculateFrame(500, intensity)
        assertEquals(0.0f, left2, "1Hz pattern should be OFF at 500ms (second half)")
        
        // Test 4 Hz (250ms period)
        val pattern4Hz = VibrationPattern.Pulse(frequencyHz = 4.0f)
        val (left3, right3) = pattern4Hz.calculateFrame(0, intensity)
        assertEquals(intensity, left3, "4Hz pattern should be ON at start")
        
        val (left4, right4) = pattern4Hz.calculateFrame(125, intensity)
        assertEquals(0.0f, left4, "4Hz pattern should be OFF at 125ms (second half of 250ms period)")
    }
    
    @Test
    fun testPulsePatternMotorValuesInValidRange() {
        val pattern = VibrationPattern.Pulse(frequencyHz = 2.0f)
        val intensities = listOf(0.0f, 0.1f, 0.3f, 0.5f, 0.7f, 0.9f, 1.0f)
        val times = listOf(0L, 100L, 250L, 400L, 500L, 1000L)
        
        for (intensity in intensities) {
            for (time in times) {
                val (left, right) = pattern.calculateFrame(time, intensity)
                assertTrue(left >= 0.0f && left <= 1.0f, "Left motor value $left should be in range [0.0, 1.0] at time $time")
                assertTrue(right >= 0.0f && right <= 1.0f, "Right motor value $right should be in range [0.0, 1.0] at time $time")
            }
        }
    }
    
    @Test
    fun testPulsePatternBothMotorsEqual() {
        val pattern = VibrationPattern.Pulse(frequencyHz = 2.0f)
        val intensities = listOf(0.0f, 0.3f, 0.6f, 1.0f)
        val times = listOf(0L, 100L, 250L, 400L, 500L)
        
        for (intensity in intensities) {
            for (time in times) {
                val (left, right) = pattern.calculateFrame(time, intensity)
                assertEquals(left, right, "Both motors should have equal values for pulse pattern at time $time")
            }
        }
    }
    
    @Test
    fun testPulsePatternDefaultFrequency() {
        val pattern = VibrationPattern.Pulse() // Should default to 2.0 Hz
        val intensity = 0.5f
        
        // With 2 Hz, period is 500ms
        val (left1, right1) = pattern.calculateFrame(0, intensity)
        assertEquals(intensity, left1, "Default frequency should be 2 Hz (ON at start)")
        
        val (left2, right2) = pattern.calculateFrame(250, intensity)
        assertEquals(0.0f, left2, "Default frequency should be 2 Hz (OFF at 250ms)")
    }
    
    @Test
    fun testPulsePatternHighFrequency() {
        val pattern = VibrationPattern.Pulse(frequencyHz = 10.0f) // 100ms period
        val intensity = 0.8f
        
        // First half (0-50ms): ON
        val (left1, right1) = pattern.calculateFrame(0, intensity)
        assertEquals(intensity, left1, "Should be ON at start")
        
        val (left2, right2) = pattern.calculateFrame(49, intensity)
        assertEquals(intensity, left2, "Should be ON at 49ms")
        
        // Second half (50-100ms): OFF
        val (left3, right3) = pattern.calculateFrame(50, intensity)
        assertEquals(0.0f, left3, "Should be OFF at 50ms")
        
        val (left4, right4) = pattern.calculateFrame(99, intensity)
        assertEquals(0.0f, left4, "Should be OFF at 99ms")
        
        // Next period starts at 100ms
        val (left5, right5) = pattern.calculateFrame(100, intensity)
        assertEquals(intensity, left5, "Should be ON at 100ms (new period)")
    }
    
    @Test
    fun testPulsePatternLowFrequency() {
        val pattern = VibrationPattern.Pulse(frequencyHz = 0.5f) // 2000ms period
        val intensity = 0.6f
        
        // First half (0-1000ms): ON
        val (left1, right1) = pattern.calculateFrame(0, intensity)
        assertEquals(intensity, left1, "Should be ON at start")
        
        val (left2, right2) = pattern.calculateFrame(999, intensity)
        assertEquals(intensity, left2, "Should be ON at 999ms")
        
        // Second half (1000-2000ms): OFF
        val (left3, right3) = pattern.calculateFrame(1000, intensity)
        assertEquals(0.0f, left3, "Should be OFF at 1000ms")
        
        val (left4, right4) = pattern.calculateFrame(1999, intensity)
        assertEquals(0.0f, left4, "Should be OFF at 1999ms")
    }
    
    @Test
    fun testPulsePatternIntensityScaling() {
        val pattern = VibrationPattern.Pulse(frequencyHz = 2.0f)
        
        // Test that different intensities scale the ON value correctly
        val intensities = listOf(0.2f, 0.5f, 0.8f, 1.0f)
        
        for (intensity in intensities) {
            // During ON phase (first half)
            val (left, right) = pattern.calculateFrame(0, intensity)
            assertEquals(intensity, left, "ON value should match intensity $intensity")
            assertEquals(intensity, right, "ON value should match intensity $intensity")
            
            // During OFF phase (second half)
            val (leftOff, rightOff) = pattern.calculateFrame(250, intensity)
            assertEquals(0.0f, leftOff, "OFF value should always be 0.0 regardless of intensity")
            assertEquals(0.0f, rightOff, "OFF value should always be 0.0 regardless of intensity")
        }
    }
    
    // WavePattern Tests
    
    @Test
    fun testWavePatternWithZeroIntensity() {
        val pattern = VibrationPattern.Wave(frequencyHz = 1.0f)
        val (left, right) = pattern.calculateFrame(0, 0.0f)
        
        assertEquals(0.0f, left, "Left motor should be 0.0 with zero intensity")
        assertEquals(0.0f, right, "Right motor should be 0.0 with zero intensity")
    }
    
    @Test
    fun testWavePatternWithMaxIntensity() {
        val pattern = VibrationPattern.Wave(frequencyHz = 1.0f)
        
        // At time 0, sine(0) = 0, normalized to 0.5, so value should be 0.5 * intensity
        val (left1, right1) = pattern.calculateFrame(0, 1.0f)
        assertEquals(0.5f, left1, 0.01f, "At time 0, wave should be at midpoint (0.5)")
        assertEquals(0.5f, right1, 0.01f, "At time 0, wave should be at midpoint (0.5)")
        
        // At quarter period (250ms for 1Hz), sine(π/2) = 1, normalized to 1.0
        val (left2, right2) = pattern.calculateFrame(250, 1.0f)
        assertEquals(1.0f, left2, 0.01f, "At quarter period, wave should be at maximum (1.0)")
        assertEquals(1.0f, right2, 0.01f, "At quarter period, wave should be at maximum (1.0)")
        
        // At half period (500ms for 1Hz), sine(π) = 0, normalized to 0.5
        val (left3, right3) = pattern.calculateFrame(500, 1.0f)
        assertEquals(0.5f, left3, 0.01f, "At half period, wave should be at midpoint (0.5)")
        assertEquals(0.5f, right3, 0.01f, "At half period, wave should be at midpoint (0.5)")
        
        // At three-quarter period (750ms for 1Hz), sine(3π/2) = -1, normalized to 0.0
        val (left4, right4) = pattern.calculateFrame(750, 1.0f)
        assertEquals(0.0f, left4, 0.01f, "At three-quarter period, wave should be at minimum (0.0)")
        assertEquals(0.0f, right4, 0.01f, "At three-quarter period, wave should be at minimum (0.0)")
    }
    
    @Test
    fun testWavePatternSmoothTransition() {
        val pattern = VibrationPattern.Wave(frequencyHz = 1.0f) // 1000ms period
        val intensity = 1.0f
        
        // Sample the wave at multiple points to verify smooth transition
        val samples = listOf(
            0L to 0.5f,      // Start: sine(0) = 0 -> 0.5
            125L to 0.85f,   // Eighth: sine(π/4) ≈ 0.707 -> 0.85
            250L to 1.0f,    // Quarter: sine(π/2) = 1 -> 1.0
            375L to 0.85f,   // Three-eighths: sine(3π/4) ≈ 0.707 -> 0.85
            500L to 0.5f,    // Half: sine(π) = 0 -> 0.5
            625L to 0.15f,   // Five-eighths: sine(5π/4) ≈ -0.707 -> 0.15
            750L to 0.0f,    // Three-quarters: sine(3π/2) = -1 -> 0.0
            875L to 0.15f    // Seven-eighths: sine(7π/4) ≈ -0.707 -> 0.15
        )
        
        for ((time, expectedValue) in samples) {
            val (left, right) = pattern.calculateFrame(time, intensity)
            assertEquals(expectedValue, left, 0.02f, "Wave value at ${time}ms should be approximately $expectedValue")
            assertEquals(expectedValue, right, 0.02f, "Wave value at ${time}ms should be approximately $expectedValue")
        }
    }
    
    @Test
    fun testWavePatternPeriodicity() {
        val pattern = VibrationPattern.Wave(frequencyHz = 1.0f) // 1000ms period
        val intensity = 0.7f
        
        // Test that pattern repeats after one period
        val (left1, right1) = pattern.calculateFrame(0, intensity)
        val (left2, right2) = pattern.calculateFrame(1000, intensity)
        
        assertEquals(left1, left2, 0.01f, "Pattern should repeat after one period")
        assertEquals(right1, right2, 0.01f, "Pattern should repeat after one period")
        
        // Test at quarter period
        val (left3, right3) = pattern.calculateFrame(250, intensity)
        val (left4, right4) = pattern.calculateFrame(1250, intensity)
        
        assertEquals(left3, left4, 0.01f, "Pattern should repeat at quarter period offset")
        assertEquals(right3, right4, 0.01f, "Pattern should repeat at quarter period offset")
    }
    
    @Test
    fun testWavePatternDifferentFrequencies() {
        val intensity = 0.8f
        
        // Test 0.5 Hz (2000ms period)
        val pattern05Hz = VibrationPattern.Wave(frequencyHz = 0.5f)
        val (left1, right1) = pattern05Hz.calculateFrame(0, intensity)
        assertEquals(0.4f, left1, 0.01f, "0.5Hz pattern should start at midpoint scaled by intensity")
        
        val (left2, right2) = pattern05Hz.calculateFrame(500, intensity)
        assertEquals(0.8f, left2, 0.01f, "0.5Hz pattern should be at max at 500ms (quarter of 2000ms period)")
        
        // Test 2 Hz (500ms period)
        val pattern2Hz = VibrationPattern.Wave(frequencyHz = 2.0f)
        val (left3, right3) = pattern2Hz.calculateFrame(0, intensity)
        assertEquals(0.4f, left3, 0.01f, "2Hz pattern should start at midpoint scaled by intensity")
        
        val (left4, right4) = pattern2Hz.calculateFrame(125, intensity)
        assertEquals(0.8f, left4, 0.01f, "2Hz pattern should be at max at 125ms (quarter of 500ms period)")
    }
    
    @Test
    fun testWavePatternIntensityScaling() {
        val pattern = VibrationPattern.Wave(frequencyHz = 1.0f)
        
        // Test that different intensities scale the wave correctly
        val intensities = listOf(0.2f, 0.5f, 0.8f, 1.0f)
        
        for (intensity in intensities) {
            // At quarter period, normalized sine value is 1.0
            val (left, right) = pattern.calculateFrame(250, intensity)
            assertEquals(intensity, left, 0.01f, "Max wave value should match intensity $intensity")
            assertEquals(intensity, right, 0.01f, "Max wave value should match intensity $intensity")
            
            // At three-quarter period, normalized sine value is 0.0
            val (leftMin, rightMin) = pattern.calculateFrame(750, intensity)
            assertEquals(0.0f, leftMin, 0.01f, "Min wave value should always be 0.0")
            assertEquals(0.0f, rightMin, 0.01f, "Min wave value should always be 0.0")
            
            // At start/half period, normalized sine value is 0.5
            val (leftMid, rightMid) = pattern.calculateFrame(0, intensity)
            assertEquals(intensity * 0.5f, leftMid, 0.01f, "Mid wave value should be half of intensity")
            assertEquals(intensity * 0.5f, rightMid, 0.01f, "Mid wave value should be half of intensity")
        }
    }
    
    @Test
    fun testWavePatternMotorValuesInValidRange() {
        val pattern = VibrationPattern.Wave(frequencyHz = 1.0f)
        val intensities = listOf(0.0f, 0.1f, 0.3f, 0.5f, 0.7f, 0.9f, 1.0f)
        val times = listOf(0L, 100L, 250L, 500L, 750L, 1000L)
        
        for (intensity in intensities) {
            for (time in times) {
                val (left, right) = pattern.calculateFrame(time, intensity)
                assertTrue(left >= 0.0f && left <= 1.0f, "Left motor value $left should be in range [0.0, 1.0] at time $time")
                assertTrue(right >= 0.0f && right <= 1.0f, "Right motor value $right should be in range [0.0, 1.0] at time $time")
            }
        }
    }
    
    @Test
    fun testWavePatternBothMotorsEqual() {
        val pattern = VibrationPattern.Wave(frequencyHz = 1.0f)
        val intensities = listOf(0.0f, 0.3f, 0.6f, 1.0f)
        val times = listOf(0L, 100L, 250L, 500L, 750L, 1000L)
        
        for (intensity in intensities) {
            for (time in times) {
                val (left, right) = pattern.calculateFrame(time, intensity)
                assertEquals(left, right, 0.001f, "Both motors should have equal values for wave pattern at time $time")
            }
        }
    }
    
    @Test
    fun testWavePatternDefaultFrequency() {
        val pattern = VibrationPattern.Wave() // Should default to 1.0 Hz
        val intensity = 0.5f
        
        // With 1 Hz, period is 1000ms
        val (left1, right1) = pattern.calculateFrame(0, intensity)
        assertEquals(0.25f, left1, 0.01f, "Default frequency should be 1 Hz (midpoint at start)")
        
        val (left2, right2) = pattern.calculateFrame(250, intensity)
        assertEquals(0.5f, left2, 0.01f, "Default frequency should be 1 Hz (max at 250ms)")
    }
    
    @Test
    fun testWavePatternHighFrequency() {
        val pattern = VibrationPattern.Wave(frequencyHz = 10.0f) // 100ms period
        val intensity = 0.8f
        
        // At time 0, should be at midpoint
        val (left1, right1) = pattern.calculateFrame(0, intensity)
        assertEquals(0.4f, left1, 0.01f, "Should be at midpoint at start")
        
        // At quarter period (25ms), should be at max
        val (left2, right2) = pattern.calculateFrame(25, intensity)
        assertEquals(0.8f, left2, 0.01f, "Should be at max at 25ms")
        
        // At half period (50ms), should be at midpoint
        val (left3, right3) = pattern.calculateFrame(50, intensity)
        assertEquals(0.4f, left3, 0.01f, "Should be at midpoint at 50ms")
        
        // At three-quarter period (75ms), should be at min
        val (left4, right4) = pattern.calculateFrame(75, intensity)
        assertEquals(0.0f, left4, 0.01f, "Should be at min at 75ms")
    }
    
    @Test
    fun testWavePatternLowFrequency() {
        val pattern = VibrationPattern.Wave(frequencyHz = 0.25f) // 4000ms period
        val intensity = 0.6f
        
        // At time 0, should be at midpoint
        val (left1, right1) = pattern.calculateFrame(0, intensity)
        assertEquals(0.3f, left1, 0.01f, "Should be at midpoint at start")
        
        // At quarter period (1000ms), should be at max
        val (left2, right2) = pattern.calculateFrame(1000, intensity)
        assertEquals(0.6f, left2, 0.01f, "Should be at max at 1000ms")
        
        // At half period (2000ms), should be at midpoint
        val (left3, right3) = pattern.calculateFrame(2000, intensity)
        assertEquals(0.3f, left3, 0.01f, "Should be at midpoint at 2000ms")
        
        // At three-quarter period (3000ms), should be at min
        val (left4, right4) = pattern.calculateFrame(3000, intensity)
        assertEquals(0.0f, left4, 0.01f, "Should be at min at 3000ms")
    }
    
    @Test
    fun testWavePatternContinuousSmoothing() {
        val pattern = VibrationPattern.Wave(frequencyHz = 1.0f)
        val intensity = 1.0f
        
        // Verify that consecutive samples show smooth progression (no jumps)
        var previousValue = 0.5f // Start value
        val timeStep = 50L // Sample every 50ms
        
        for (time in 0L..1000L step timeStep) {
            val (left, _) = pattern.calculateFrame(time, intensity)
            
            // Verify no large jumps (smooth transition)
            val delta = kotlin.math.abs(left - previousValue)
            assertTrue(delta <= 0.3f, "Wave should transition smoothly, but jumped by $delta at time $time")
            
            previousValue = left
        }
    }
}
