package com.seashore.zoonzoon.gamepad.engine

import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertEquals

class TouchHapticsTest {

    @Test
    fun topOfWaveDrivesLeftMotor() {
        val (left, right) = touchHapticMotors(normalizedY = 0f, intensity = 1f)
        assertEquals(TouchHapticMaxScale, left, 0.001f)
        assertEquals(0f, right, 0.001f)
    }

    @Test
    fun bottomOfWaveDrivesRightMotor() {
        val (left, right) = touchHapticMotors(normalizedY = 1f, intensity = 1f)
        assertEquals(0f, left, 0.001f)
        assertEquals(TouchHapticMaxScale, right, 0.001f)
    }

    @Test
    fun midWaveSplitsHandlesEvenly() {
        val (left, right) = touchHapticMotors(normalizedY = 0.5f, intensity = 1f)
        val expected = TouchHapticMaxScale * sqrt(0.5f)
        assertEquals(expected, left, 0.001f)
        assertEquals(expected, right, 0.001f)
    }

    @Test
    fun intensityScalesPeakOutput() {
        val (left, right) = touchHapticMotors(normalizedY = 0f, intensity = 0.5f)
        assertEquals(TouchHapticMaxScale * 0.5f, left, 0.001f)
        assertEquals(0f, right, 0.001f)
    }
}
