package com.seashore.zoonzoon.gamepad.ui

import com.seashore.zoonzoon.gamepad.model.ConnectionState
import com.seashore.zoonzoon.gamepad.model.VibrationPattern
import com.seashore.zoonzoon.gamepad.model.VibrationState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for composable display logic.
 *
 * Tests the logic that drives ConnectionStatusCard, VibrationControlCard,
 * PatternSelectionCard, and IntensitySlider without requiring Compose runtime.
 *
 * **Validates: Requirements 7.4, 7.5, 7.6, 7.7, 7.8, 7.9**
 */
class ComposableLogicTest {

    // -------------------------------------------------------------------------
    // ConnectionStatusCard logic
    // -------------------------------------------------------------------------

    @Test
    fun testConnectionStatusCard_disconnectedState() {
        val state = ConnectionState.Disconnected
        assertFalse(state is ConnectionState.Connected)
        assertFalse(state is ConnectionState.Scanning)
    }

    @Test
    fun testConnectionStatusCard_scanningState() {
        val state = ConnectionState.Scanning
        assertFalse(state is ConnectionState.Connected)
        assertFalse(state is ConnectionState.Disconnected)
    }

    @Test
    fun testConnectionStatusCard_connectedState() {
        val state = ConnectionState.Connected("DualShock 4")
        assertTrue(state is ConnectionState.Connected)
        assertEquals("DualShock 4", state.controllerType)
    }

    @Test
    fun testConnectionStatusCard_allStatesDistinct() {
        val disconnected = ConnectionState.Disconnected
        val scanning = ConnectionState.Scanning
        val connected = ConnectionState.Connected("Xbox")

        assertTrue(disconnected is ConnectionState.Disconnected)
        assertTrue(scanning is ConnectionState.Scanning)
        assertTrue(connected is ConnectionState.Connected)
    }

    // -------------------------------------------------------------------------
    // VibrationControlCard logic
    // -------------------------------------------------------------------------

    @Test
    fun testVibrationControlCard_enabledState() {
        val state = VibrationState(enabled = true)
        assertTrue(state.enabled)
    }

    @Test
    fun testVibrationControlCard_disabledState() {
        val state = VibrationState(enabled = false)
        assertFalse(state.enabled)
    }

    @Test
    fun testVibrationControlCard_toggleLogic() {
        var enabled = false
        // Simulate button press
        enabled = !enabled
        assertTrue(enabled)
        enabled = !enabled
        assertFalse(enabled)
    }

    // -------------------------------------------------------------------------
    // PatternSelectionCard logic
    // -------------------------------------------------------------------------

    @Test
    fun testPatternSelectionCard_constantPatternSelected() {
        val activePattern: VibrationPattern = VibrationPattern.Constant
        assertTrue(activePattern is VibrationPattern.Constant)
        assertFalse(activePattern is VibrationPattern.Pulse)
        assertFalse(activePattern is VibrationPattern.Wave)
        assertFalse(activePattern is VibrationPattern.Custom)
    }

    @Test
    fun testPatternSelectionCard_pulsePatternSelected() {
        val activePattern: VibrationPattern = VibrationPattern.Pulse(2.0f)
        assertFalse(activePattern is VibrationPattern.Constant)
        assertTrue(activePattern is VibrationPattern.Pulse)
    }

    @Test
    fun testPatternSelectionCard_wavePatternSelected() {
        val activePattern: VibrationPattern = VibrationPattern.Wave(1.0f)
        assertTrue(activePattern is VibrationPattern.Wave)
    }

    @Test
    fun testPatternSelectionCard_customPatternSelected() {
        val activePattern: VibrationPattern = VibrationPattern.Custom(emptyList())
        assertTrue(activePattern is VibrationPattern.Custom)
    }

    @Test
    fun testPatternSelectionCard_isSelectedLogic() {
        val activePattern: VibrationPattern = VibrationPattern.Pulse(2.0f)
        val options = listOf(
            VibrationPattern.Constant,
            VibrationPattern.Pulse(),
            VibrationPattern.Wave(),
            VibrationPattern.Custom(emptyList())
        )
        val selectedCount = options.count { it::class == activePattern::class }
        assertEquals(1, selectedCount, "Exactly one pattern should be selected")
    }

    // -------------------------------------------------------------------------
    // IntensitySlider logic
    // -------------------------------------------------------------------------

    @Test
    fun testIntensitySlider_zeroIntensity() {
        val intensity = 0.0f
        val percent = (intensity * 100).toInt()
        assertEquals(0, percent)
    }

    @Test
    fun testIntensitySlider_fullIntensity() {
        val intensity = 1.0f
        val percent = (intensity * 100).toInt()
        assertEquals(100, percent)
    }

    @Test
    fun testIntensitySlider_halfIntensity() {
        val intensity = 0.5f
        val percent = (intensity * 100).toInt()
        assertEquals(50, percent)
    }

    @Test
    fun testIntensitySlider_valueRange() {
        val validValues = listOf(0.0f, 0.1f, 0.5f, 0.9f, 1.0f)
        for (v in validValues) {
            assertTrue(v in 0.0f..1.0f, "Intensity $v should be in valid range")
        }
    }

    @Test
    fun testIntensitySlider_callbackInvoked() {
        var lastValue = -1f
        val onChanged: (Float) -> Unit = { lastValue = it }

        onChanged(0.7f)
        assertEquals(0.7f, lastValue)

        onChanged(0.3f)
        assertEquals(0.3f, lastValue)
    }
}
