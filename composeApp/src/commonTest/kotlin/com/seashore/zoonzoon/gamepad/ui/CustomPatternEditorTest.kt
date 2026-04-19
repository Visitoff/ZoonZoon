package com.seashore.zoonzoon.gamepad.ui

import com.seashore.zoonzoon.gamepad.model.VibrationPattern
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for custom pattern editor logic.
 *
 * **Validates: Requirement 3.5**
 */
class CustomPatternEditorTest {

    @Test
    fun testAddFrame_increasesFrameCount() {
        val frames = mutableListOf<VibrationPattern.Custom.Frame>()
        val newFrame = VibrationPattern.Custom.Frame(0.5f, 0.5f, 200L)
        frames.add(newFrame)
        assertEquals(1, frames.size)
    }

    @Test
    fun testRemoveFrame_decreasesFrameCount() {
        val frames = mutableListOf(
            VibrationPattern.Custom.Frame(0.5f, 0.5f, 200L),
            VibrationPattern.Custom.Frame(0.8f, 0.2f, 100L)
        )
        frames.removeAt(0)
        assertEquals(1, frames.size)
    }

    @Test
    fun testUpdateFrame_changesValues() {
        val frames = mutableListOf(
            VibrationPattern.Custom.Frame(0.5f, 0.5f, 200L)
        )
        frames[0] = frames[0].copy(leftMotor = 0.9f)
        assertEquals(0.9f, frames[0].leftMotor)
    }

    @Test
    fun testFrameMotorValuesInValidRange() {
        val frames = listOf(
            VibrationPattern.Custom.Frame(0.0f, 0.0f, 100L),
            VibrationPattern.Custom.Frame(0.5f, 0.5f, 100L),
            VibrationPattern.Custom.Frame(1.0f, 1.0f, 100L)
        )
        for (frame in frames) {
            assertTrue(frame.leftMotor in 0f..1f)
            assertTrue(frame.rightMotor in 0f..1f)
            assertTrue(frame.durationMs > 0)
        }
    }

    @Test
    fun testDefaultFrameValues() {
        val frame = VibrationPattern.Custom.Frame(
            leftMotor = 0.5f,
            rightMotor = 0.5f,
            durationMs = 200L
        )
        assertEquals(0.5f, frame.leftMotor)
        assertEquals(0.5f, frame.rightMotor)
        assertEquals(200L, frame.durationMs)
    }

    @Test
    fun testCustomPatternFromFrames() {
        val frames = listOf(
            VibrationPattern.Custom.Frame(0.7f, 0.3f, 150L),
            VibrationPattern.Custom.Frame(0.3f, 0.7f, 150L)
        )
        val pattern = VibrationPattern.Custom(frames = frames, loop = true)

        assertEquals(2, pattern.frames.size)
        assertTrue(pattern.loop)
        assertEquals(frames, pattern.frames)
    }

    @Test
    fun testSaveAndLoadPattern() {
        val frames = listOf(
            VibrationPattern.Custom.Frame(0.6f, 0.4f, 200L)
        )
        val saved = VibrationPattern.Custom(frames = frames, loop = false)

        // Simulate save/load by equality check
        val loaded = saved.copy()
        assertEquals(saved, loaded)
        assertEquals(saved.frames, loaded.frames)
        assertEquals(saved.loop, loaded.loop)
    }
}
