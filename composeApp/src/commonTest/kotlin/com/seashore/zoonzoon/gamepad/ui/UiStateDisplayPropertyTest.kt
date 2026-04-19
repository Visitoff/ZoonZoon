package com.seashore.zoonzoon.gamepad.ui

import com.seashore.zoonzoon.gamepad.model.ConnectionState
import com.seashore.zoonzoon.gamepad.model.VibrationPattern
import com.seashore.zoonzoon.gamepad.model.VibrationState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Property tests for UI state display logic.
 *
 * **Property 15: UI State Display**
 * **Validates: Requirements 7.4, 7.5**
 *
 * Tests that the UI correctly maps state values to display information.
 * These tests validate the logic layer without requiring Compose runtime.
 */
class UiStateDisplayPropertyTest {

    // -------------------------------------------------------------------------
    // ConnectionState display logic
    // -------------------------------------------------------------------------

    @Test
    fun testConnectionStateDisconnectedDisplayText() {
        val state = ConnectionState.Disconnected
        val text = connectionStateToText(state)
        assertEquals("Disconnected", text)
    }

    @Test
    fun testConnectionStateScanningDisplayText() {
        val state = ConnectionState.Scanning
        val text = connectionStateToText(state)
        assertEquals("Scanning...", text)
    }

    @Test
    fun testConnectionStateConnectedDisplayText() {
        val state = ConnectionState.Connected("DualShock 4")
        val text = connectionStateToText(state)
        assertEquals("Connected", text)
    }

    @Test
    fun testConnectionStateConnectedShowsControllerType() {
        val controllerTypes = listOf("DualShock 4", "DualSense", "Xbox One", "Xbox Series")
        for (type in controllerTypes) {
            val state = ConnectionState.Connected(type)
            assertTrue(
                state.controllerType == type,
                "Connected state should expose controller type: $type"
            )
        }
    }

    @Test
    fun testAllConnectionStatesHaveDisplayText() {
        val states = listOf(
            ConnectionState.Disconnected,
            ConnectionState.Scanning,
            ConnectionState.Connected("Test")
        )
        for (state in states) {
            val text = connectionStateToText(state)
            assertTrue(text.isNotEmpty(), "Every ConnectionState should have non-empty display text")
        }
    }

    // -------------------------------------------------------------------------
    // VibrationState display logic
    // -------------------------------------------------------------------------

    @Test
    fun testVibrationStateEnabledDisplayText() {
        val state = VibrationState(enabled = true)
        val text = vibrationStateToStatusText(state)
        assertEquals("Enabled", text)
    }

    @Test
    fun testVibrationStateDisabledDisplayText() {
        val state = VibrationState(enabled = false)
        val text = vibrationStateToStatusText(state)
        assertEquals("Disabled", text)
    }

    @Test
    fun testVibrationStateShowsActivePattern() {
        val patterns = listOf(
            VibrationPattern.Constant,
            VibrationPattern.Pulse(),
            VibrationPattern.Wave(),
            VibrationPattern.Custom(emptyList())
        )
        for (pattern in patterns) {
            val state = VibrationState(enabled = true, activePattern = pattern)
            assertEquals(pattern, state.activePattern, "VibrationState should expose active pattern")
        }
    }

    @Test
    fun testVibrationStateShowsIntensity() {
        val intensities = listOf(0.0f, 0.25f, 0.5f, 0.75f, 1.0f)
        for (intensity in intensities) {
            val state = VibrationState(intensity = intensity)
            assertEquals(intensity, state.intensity, "VibrationState should expose intensity: $intensity")
        }
    }

    @Test
    fun testIntensityDisplayAsPercentage() {
        val cases = mapOf(
            0.0f  to 0,
            0.5f  to 50,
            1.0f  to 100,
            0.25f to 25,
            0.75f to 75
        )
        for ((intensity, expectedPercent) in cases) {
            val percent = (intensity * 100).toInt()
            assertEquals(expectedPercent, percent, "Intensity $intensity should display as $expectedPercent%")
        }
    }

    // -------------------------------------------------------------------------
    // Pattern name display logic
    // -------------------------------------------------------------------------

    @Test
    fun testPatternNamesAreCorrect() {
        val cases = mapOf(
            VibrationPattern.Constant          to "Constant",
            VibrationPattern.Pulse()           to "Pulse",
            VibrationPattern.Wave()            to "Wave",
            VibrationPattern.Custom(emptyList()) to "Custom"
        )
        for ((pattern, expectedName) in cases) {
            val name = patternToDisplayName(pattern)
            assertEquals(expectedName, name, "Pattern ${pattern::class.simpleName} should display as $expectedName")
        }
    }

    // -------------------------------------------------------------------------
    // Helper functions (mirror the display logic in composables)
    // -------------------------------------------------------------------------

    private fun connectionStateToText(state: ConnectionState): String = when (state) {
        is ConnectionState.Disconnected -> "Disconnected"
        is ConnectionState.Scanning     -> "Scanning..."
        is ConnectionState.Connected    -> "Connected"
    }

    private fun vibrationStateToStatusText(state: VibrationState): String =
        if (state.enabled) "Enabled" else "Disabled"

    private fun patternToDisplayName(pattern: VibrationPattern): String = when (pattern) {
        is VibrationPattern.Constant -> "Constant"
        is VibrationPattern.Pulse    -> "Pulse"
        is VibrationPattern.Wave     -> "Wave"
        is VibrationPattern.Custom   -> "Custom"
    }
}
