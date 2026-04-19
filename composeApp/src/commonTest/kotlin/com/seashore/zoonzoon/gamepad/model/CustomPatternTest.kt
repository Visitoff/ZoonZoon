package com.seashore.zoonzoon.gamepad.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Comprehensive unit tests for VibrationPattern.Custom.
 * 
 * **Validates: Requirements 3.5**
 * 
 * Tests custom pattern support including:
 * - Frame sequence execution
 * - Intensity scaling
 * - Looping behavior
 * - Edge cases (empty frames, single frame, etc.)
 */
class CustomPatternTest {
    
    // Basic Functionality Tests
    
    @Test
    fun testCustomPatternWithSingleFrame() {
        val frames = listOf(
            VibrationPattern.Custom.Frame(
                leftMotor = 0.8f,
                rightMotor = 0.6f,
                durationMs = 100
            )
        )
        val pattern = VibrationPattern.Custom(frames = frames, loop = false)
        
        // Within frame duration
        val (left1, right1) = pattern.calculateFrame(0, 1.0f)
        assertEquals(0.8f, left1, "Left motor should match frame value")
        assertEquals(0.6f, right1, "Right motor should match frame value")
        
        val (left2, right2) = pattern.calculateFrame(50, 1.0f)
        assertEquals(0.8f, left2, "Left motor should remain constant within frame")
        assertEquals(0.6f, right2, "Right motor should remain constant within frame")
        
        val (left3, right3) = pattern.calculateFrame(99, 1.0f)
        assertEquals(0.8f, left3, "Left motor should remain constant until frame end")
        assertEquals(0.6f, right3, "Right motor should remain constant until frame end")
        
        // After frame duration (no loop)
        val (left4, right4) = pattern.calculateFrame(100, 1.0f)
        assertEquals(0.0f, left4, "Left motor should be 0.0 after pattern ends (no loop)")
        assertEquals(0.0f, right4, "Right motor should be 0.0 after pattern ends (no loop)")
    }
    
    @Test
    fun testCustomPatternWithMultipleFrames() {
        val frames = listOf(
            VibrationPattern.Custom.Frame(leftMotor = 1.0f, rightMotor = 0.0f, durationMs = 100),
            VibrationPattern.Custom.Frame(leftMotor = 0.0f, rightMotor = 1.0f, durationMs = 100),
            VibrationPattern.Custom.Frame(leftMotor = 0.5f, rightMotor = 0.5f, durationMs = 100)
        )
        val pattern = VibrationPattern.Custom(frames = frames, loop = false)
        
        // First frame (0-100ms)
        val (left1, right1) = pattern.calculateFrame(0, 1.0f)
        assertEquals(1.0f, left1, "First frame: left motor should be 1.0")
        assertEquals(0.0f, right1, "First frame: right motor should be 0.0")
        
        val (left2, right2) = pattern.calculateFrame(50, 1.0f)
        assertEquals(1.0f, left2, "First frame at 50ms: left motor should be 1.0")
        assertEquals(0.0f, right2, "First frame at 50ms: right motor should be 0.0")
        
        // Second frame (100-200ms)
        val (left3, right3) = pattern.calculateFrame(100, 1.0f)
        assertEquals(0.0f, left3, "Second frame: left motor should be 0.0")
        assertEquals(1.0f, right3, "Second frame: right motor should be 1.0")
        
        val (left4, right4) = pattern.calculateFrame(150, 1.0f)
        assertEquals(0.0f, left4, "Second frame at 150ms: left motor should be 0.0")
        assertEquals(1.0f, right4, "Second frame at 150ms: right motor should be 1.0")
        
        // Third frame (200-300ms)
        val (left5, right5) = pattern.calculateFrame(200, 1.0f)
        assertEquals(0.5f, left5, "Third frame: left motor should be 0.5")
        assertEquals(0.5f, right5, "Third frame: right motor should be 0.5")
        
        val (left6, right6) = pattern.calculateFrame(250, 1.0f)
        assertEquals(0.5f, left6, "Third frame at 250ms: left motor should be 0.5")
        assertEquals(0.5f, right6, "Third frame at 250ms: right motor should be 0.5")
        
        // After all frames (300ms+)
        val (left7, right7) = pattern.calculateFrame(300, 1.0f)
        assertEquals(0.0f, left7, "After pattern ends: left motor should be 0.0")
        assertEquals(0.0f, right7, "After pattern ends: right motor should be 0.0")
    }
    
    @Test
    fun testCustomPatternWithEmptyFrames() {
        val pattern = VibrationPattern.Custom(frames = emptyList(), loop = false)
        
        val (left, right) = pattern.calculateFrame(0, 1.0f)
        assertEquals(0.0f, left, "Empty pattern should return 0.0 for left motor")
        assertEquals(0.0f, right, "Empty pattern should return 0.0 for right motor")
        
        val (left2, right2) = pattern.calculateFrame(1000, 1.0f)
        assertEquals(0.0f, left2, "Empty pattern should return 0.0 at any time")
        assertEquals(0.0f, right2, "Empty pattern should return 0.0 at any time")
    }
    
    // Intensity Scaling Tests
    
    @Test
    fun testCustomPatternWithZeroIntensity() {
        val frames = listOf(
            VibrationPattern.Custom.Frame(leftMotor = 1.0f, rightMotor = 0.8f, durationMs = 100)
        )
        val pattern = VibrationPattern.Custom(frames = frames, loop = false)
        
        val (left, right) = pattern.calculateFrame(0, 0.0f)
        assertEquals(0.0f, left, "Left motor should be 0.0 with zero intensity")
        assertEquals(0.0f, right, "Right motor should be 0.0 with zero intensity")
    }
    
    @Test
    fun testCustomPatternWithMaxIntensity() {
        val frames = listOf(
            VibrationPattern.Custom.Frame(leftMotor = 0.6f, rightMotor = 0.8f, durationMs = 100)
        )
        val pattern = VibrationPattern.Custom(frames = frames, loop = false)
        
        val (left, right) = pattern.calculateFrame(0, 1.0f)
        assertEquals(0.6f, left, "Left motor should match frame value with max intensity")
        assertEquals(0.8f, right, "Right motor should match frame value with max intensity")
    }
    
    @Test
    fun testCustomPatternIntensityScaling() {
        val frames = listOf(
            VibrationPattern.Custom.Frame(leftMotor = 1.0f, rightMotor = 0.8f, durationMs = 100)
        )
        val pattern = VibrationPattern.Custom(frames = frames, loop = false)
        
        // Test various intensity values
        val intensities = listOf(0.0f, 0.25f, 0.5f, 0.75f, 1.0f)
        
        for (intensity in intensities) {
            val (left, right) = pattern.calculateFrame(0, intensity)
            assertEquals(1.0f * intensity, left, 0.001f, "Left motor should scale by intensity $intensity")
            assertEquals(0.8f * intensity, right, 0.001f, "Right motor should scale by intensity $intensity")
        }
    }
    
    @Test
    fun testCustomPatternIntensityScalingAcrossFrames() {
        val frames = listOf(
            VibrationPattern.Custom.Frame(leftMotor = 1.0f, rightMotor = 0.0f, durationMs = 100),
            VibrationPattern.Custom.Frame(leftMotor = 0.5f, rightMotor = 0.5f, durationMs = 100),
            VibrationPattern.Custom.Frame(leftMotor = 0.0f, rightMotor = 1.0f, durationMs = 100)
        )
        val pattern = VibrationPattern.Custom(frames = frames, loop = false)
        val intensity = 0.5f
        
        // First frame
        val (left1, right1) = pattern.calculateFrame(0, intensity)
        assertEquals(0.5f, left1, 0.001f, "First frame left should scale by intensity")
        assertEquals(0.0f, right1, 0.001f, "First frame right should scale by intensity")
        
        // Second frame
        val (left2, right2) = pattern.calculateFrame(100, intensity)
        assertEquals(0.25f, left2, 0.001f, "Second frame left should scale by intensity")
        assertEquals(0.25f, right2, 0.001f, "Second frame right should scale by intensity")
        
        // Third frame
        val (left3, right3) = pattern.calculateFrame(200, intensity)
        assertEquals(0.0f, left3, 0.001f, "Third frame left should scale by intensity")
        assertEquals(0.5f, right3, 0.001f, "Third frame right should scale by intensity")
    }
    
    // Looping Behavior Tests
    
    @Test
    fun testCustomPatternLoopingEnabled() {
        val frames = listOf(
            VibrationPattern.Custom.Frame(leftMotor = 1.0f, rightMotor = 0.0f, durationMs = 100),
            VibrationPattern.Custom.Frame(leftMotor = 0.0f, rightMotor = 1.0f, durationMs = 100)
        )
        val pattern = VibrationPattern.Custom(frames = frames, loop = true)
        
        // First iteration (0-200ms)
        val (left1, right1) = pattern.calculateFrame(0, 1.0f)
        assertEquals(1.0f, left1, "First frame in first iteration")
        assertEquals(0.0f, right1, "First frame in first iteration")
        
        val (left2, right2) = pattern.calculateFrame(100, 1.0f)
        assertEquals(0.0f, left2, "Second frame in first iteration")
        assertEquals(1.0f, right2, "Second frame in first iteration")
        
        // Second iteration (200-400ms) - should loop back to first frame
        val (left3, right3) = pattern.calculateFrame(200, 1.0f)
        assertEquals(1.0f, left3, "First frame in second iteration (looped)")
        assertEquals(0.0f, right3, "First frame in second iteration (looped)")
        
        val (left4, right4) = pattern.calculateFrame(300, 1.0f)
        assertEquals(0.0f, left4, "Second frame in second iteration (looped)")
        assertEquals(1.0f, right4, "Second frame in second iteration (looped)")
        
        // Third iteration (400-600ms)
        val (left5, right5) = pattern.calculateFrame(400, 1.0f)
        assertEquals(1.0f, left5, "First frame in third iteration (looped)")
        assertEquals(0.0f, right5, "First frame in third iteration (looped)")
    }
    
    @Test
    fun testCustomPatternLoopingDisabled() {
        val frames = listOf(
            VibrationPattern.Custom.Frame(leftMotor = 1.0f, rightMotor = 0.0f, durationMs = 100),
            VibrationPattern.Custom.Frame(leftMotor = 0.0f, rightMotor = 1.0f, durationMs = 100)
        )
        val pattern = VibrationPattern.Custom(frames = frames, loop = false)
        
        // Within pattern duration (0-200ms)
        val (left1, right1) = pattern.calculateFrame(0, 1.0f)
        assertEquals(1.0f, left1, "First frame")
        assertEquals(0.0f, right1, "First frame")
        
        val (left2, right2) = pattern.calculateFrame(100, 1.0f)
        assertEquals(0.0f, left2, "Second frame")
        assertEquals(1.0f, right2, "Second frame")
        
        // After pattern duration (200ms+) - should return 0.0
        val (left3, right3) = pattern.calculateFrame(200, 1.0f)
        assertEquals(0.0f, left3, "After pattern ends (no loop)")
        assertEquals(0.0f, right3, "After pattern ends (no loop)")
        
        val (left4, right4) = pattern.calculateFrame(1000, 1.0f)
        assertEquals(0.0f, left4, "Long after pattern ends (no loop)")
        assertEquals(0.0f, right4, "Long after pattern ends (no loop)")
    }
    
    @Test
    fun testCustomPatternLoopingWithSingleFrame() {
        val frames = listOf(
            VibrationPattern.Custom.Frame(leftMotor = 0.7f, rightMotor = 0.3f, durationMs = 100)
        )
        val pattern = VibrationPattern.Custom(frames = frames, loop = true)
        
        // Should repeat the same frame indefinitely
        val times = listOf(0L, 50L, 100L, 150L, 200L, 500L, 1000L)
        
        for (time in times) {
            val (left, right) = pattern.calculateFrame(time, 1.0f)
            assertEquals(0.7f, left, 0.001f, "Left motor should be constant at time $time (looping)")
            assertEquals(0.3f, right, 0.001f, "Right motor should be constant at time $time (looping)")
        }
    }
    
    @Test
    fun testCustomPatternLoopingWithEmptyFrames() {
        val pattern = VibrationPattern.Custom(frames = emptyList(), loop = true)
        
        // Should always return 0.0 even with looping enabled
        val (left1, right1) = pattern.calculateFrame(0, 1.0f)
        assertEquals(0.0f, left1, "Empty pattern with loop should return 0.0")
        assertEquals(0.0f, right1, "Empty pattern with loop should return 0.0")
        
        val (left2, right2) = pattern.calculateFrame(1000, 1.0f)
        assertEquals(0.0f, left2, "Empty pattern with loop should return 0.0 at any time")
        assertEquals(0.0f, right2, "Empty pattern with loop should return 0.0 at any time")
    }
    
    // Edge Cases and Validation Tests
    
    @Test
    fun testCustomPatternWithVaryingFrameDurations() {
        val frames = listOf(
            VibrationPattern.Custom.Frame(leftMotor = 1.0f, rightMotor = 0.0f, durationMs = 50),
            VibrationPattern.Custom.Frame(leftMotor = 0.5f, rightMotor = 0.5f, durationMs = 150),
            VibrationPattern.Custom.Frame(leftMotor = 0.0f, rightMotor = 1.0f, durationMs = 100)
        )
        val pattern = VibrationPattern.Custom(frames = frames, loop = false)
        
        // First frame (0-50ms)
        val (left1, right1) = pattern.calculateFrame(0, 1.0f)
        assertEquals(1.0f, left1, "First frame (50ms duration)")
        assertEquals(0.0f, right1, "First frame (50ms duration)")
        
        val (left2, right2) = pattern.calculateFrame(49, 1.0f)
        assertEquals(1.0f, left2, "Still in first frame at 49ms")
        assertEquals(0.0f, right2, "Still in first frame at 49ms")
        
        // Second frame (50-200ms)
        val (left3, right3) = pattern.calculateFrame(50, 1.0f)
        assertEquals(0.5f, left3, "Second frame (150ms duration)")
        assertEquals(0.5f, right3, "Second frame (150ms duration)")
        
        val (left4, right4) = pattern.calculateFrame(100, 1.0f)
        assertEquals(0.5f, left4, "Still in second frame at 100ms")
        assertEquals(0.5f, right4, "Still in second frame at 100ms")
        
        val (left5, right5) = pattern.calculateFrame(199, 1.0f)
        assertEquals(0.5f, left5, "Still in second frame at 199ms")
        assertEquals(0.5f, right5, "Still in second frame at 199ms")
        
        // Third frame (200-300ms)
        val (left6, right6) = pattern.calculateFrame(200, 1.0f)
        assertEquals(0.0f, left6, "Third frame (100ms duration)")
        assertEquals(1.0f, right6, "Third frame (100ms duration)")
        
        val (left7, right7) = pattern.calculateFrame(250, 1.0f)
        assertEquals(0.0f, left7, "Still in third frame at 250ms")
        assertEquals(1.0f, right7, "Still in third frame at 250ms")
    }
    
    @Test
    fun testCustomPatternMotorValuesInValidRange() {
        val frames = listOf(
            VibrationPattern.Custom.Frame(leftMotor = 0.0f, rightMotor = 0.0f, durationMs = 100),
            VibrationPattern.Custom.Frame(leftMotor = 0.3f, rightMotor = 0.7f, durationMs = 100),
            VibrationPattern.Custom.Frame(leftMotor = 1.0f, rightMotor = 1.0f, durationMs = 100)
        )
        val pattern = VibrationPattern.Custom(frames = frames, loop = true)
        
        val intensities = listOf(0.0f, 0.5f, 1.0f)
        val times = listOf(0L, 50L, 100L, 150L, 200L, 250L, 300L, 350L)
        
        for (intensity in intensities) {
            for (time in times) {
                val (left, right) = pattern.calculateFrame(time, intensity)
                assertTrue(left >= 0.0f && left <= 1.0f, 
                    "Left motor value $left should be in range [0.0, 1.0] at time $time with intensity $intensity")
                assertTrue(right >= 0.0f && right <= 1.0f, 
                    "Right motor value $right should be in range [0.0, 1.0] at time $time with intensity $intensity")
            }
        }
    }
    
    @Test
    fun testCustomPatternWithAsymmetricMotorValues() {
        val frames = listOf(
            VibrationPattern.Custom.Frame(leftMotor = 1.0f, rightMotor = 0.0f, durationMs = 100),
            VibrationPattern.Custom.Frame(leftMotor = 0.0f, rightMotor = 1.0f, durationMs = 100)
        )
        val pattern = VibrationPattern.Custom(frames = frames, loop = false)
        
        // First frame: left high, right low
        val (left1, right1) = pattern.calculateFrame(0, 1.0f)
        assertEquals(1.0f, left1, "Left motor should be high in first frame")
        assertEquals(0.0f, right1, "Right motor should be low in first frame")
        
        // Second frame: left low, right high
        val (left2, right2) = pattern.calculateFrame(100, 1.0f)
        assertEquals(0.0f, left2, "Left motor should be low in second frame")
        assertEquals(1.0f, right2, "Right motor should be high in second frame")
    }
    
    @Test
    fun testCustomPatternWithZeroDurationFrame() {
        val frames = listOf(
            VibrationPattern.Custom.Frame(leftMotor = 1.0f, rightMotor = 0.0f, durationMs = 0),
            VibrationPattern.Custom.Frame(leftMotor = 0.0f, rightMotor = 1.0f, durationMs = 100)
        )
        val pattern = VibrationPattern.Custom(frames = frames, loop = false)
        
        // Zero-duration frame should be skipped immediately
        val (left, right) = pattern.calculateFrame(0, 1.0f)
        assertEquals(0.0f, left, "Should skip to second frame")
        assertEquals(1.0f, right, "Should skip to second frame")
    }
    
    @Test
    fun testCustomPatternComplexSequence() {
        // Create a complex pattern with multiple frames
        val frames = listOf(
            VibrationPattern.Custom.Frame(leftMotor = 1.0f, rightMotor = 0.0f, durationMs = 100),
            VibrationPattern.Custom.Frame(leftMotor = 0.8f, rightMotor = 0.2f, durationMs = 100),
            VibrationPattern.Custom.Frame(leftMotor = 0.6f, rightMotor = 0.4f, durationMs = 100),
            VibrationPattern.Custom.Frame(leftMotor = 0.4f, rightMotor = 0.6f, durationMs = 100),
            VibrationPattern.Custom.Frame(leftMotor = 0.2f, rightMotor = 0.8f, durationMs = 100),
            VibrationPattern.Custom.Frame(leftMotor = 0.0f, rightMotor = 1.0f, durationMs = 100)
        )
        val pattern = VibrationPattern.Custom(frames = frames, loop = true)
        val intensity = 0.5f
        
        // Test each frame
        val expectedValues = listOf(
            0L to Pair(0.5f, 0.0f),
            100L to Pair(0.4f, 0.1f),
            200L to Pair(0.3f, 0.2f),
            300L to Pair(0.2f, 0.3f),
            400L to Pair(0.1f, 0.4f),
            500L to Pair(0.0f, 0.5f)
        )
        
        for ((time, expected) in expectedValues) {
            val (left, right) = pattern.calculateFrame(time, intensity)
            assertEquals(expected.first, left, 0.001f, "Left motor at time $time")
            assertEquals(expected.second, right, 0.001f, "Right motor at time $time")
        }
        
        // Test looping - should repeat from start at 600ms
        val (leftLoop, rightLoop) = pattern.calculateFrame(600, intensity)
        assertEquals(0.5f, leftLoop, 0.001f, "Should loop back to first frame")
        assertEquals(0.0f, rightLoop, 0.001f, "Should loop back to first frame")
    }
    
    @Test
    fun testCustomPatternDefaultLoopBehavior() {
        val frames = listOf(
            VibrationPattern.Custom.Frame(leftMotor = 0.5f, rightMotor = 0.5f, durationMs = 100)
        )
        // Default constructor should have loop = true
        val pattern = VibrationPattern.Custom(frames = frames)
        
        // Test that it loops by default
        val (left1, right1) = pattern.calculateFrame(0, 1.0f)
        assertEquals(0.5f, left1, "First iteration")
        assertEquals(0.5f, right1, "First iteration")
        
        val (left2, right2) = pattern.calculateFrame(100, 1.0f)
        assertEquals(0.5f, left2, "Second iteration (should loop)")
        assertEquals(0.5f, right2, "Second iteration (should loop)")
        
        val (left3, right3) = pattern.calculateFrame(500, 1.0f)
        assertEquals(0.5f, left3, "Multiple iterations (should loop)")
        assertEquals(0.5f, right3, "Multiple iterations (should loop)")
    }
    
    @Test
    fun testCustomPatternLongDuration() {
        val frames = listOf(
            VibrationPattern.Custom.Frame(leftMotor = 0.3f, rightMotor = 0.7f, durationMs = 5000)
        )
        val pattern = VibrationPattern.Custom(frames = frames, loop = false)
        
        // Test at various points within the long duration
        val times = listOf(0L, 1000L, 2500L, 4999L)
        
        for (time in times) {
            val (left, right) = pattern.calculateFrame(time, 1.0f)
            assertEquals(0.3f, left, 0.001f, "Left motor should be constant at time $time")
            assertEquals(0.7f, right, 0.001f, "Right motor should be constant at time $time")
        }
        
        // After duration
        val (left, right) = pattern.calculateFrame(5000, 1.0f)
        assertEquals(0.0f, left, "Should be 0.0 after long duration ends")
        assertEquals(0.0f, right, "Should be 0.0 after long duration ends")
    }
}
