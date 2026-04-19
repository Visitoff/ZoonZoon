package com.seashore.zoonzoon.gamepad.viewmodel

import com.seashore.zoonzoon.gamepad.engine.FakePlatformGamepadController
import com.seashore.zoonzoon.gamepad.engine.VibrationEngine
import com.seashore.zoonzoon.gamepad.model.VibrationPattern
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Property-based tests for intensity update command.
 *
 * **Validates: Requirement 4.1**
 *
 * Property 8: Intensity Update Command
 * For any valid intensity value between 0.0 and 1.0, when the user adjusts intensity,
 * the ViewModel SHALL update the VibrationEngine with the new intensity value.
 *
 * This test validates that the ViewModel correctly delegates intensity updates to the
 * VibrationEngine and validates intensity bounds.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class IntensityUpdateCommandPropertyTest {

    /**
     * Property: setIntensity delegates to VibrationEngine for all valid intensities
     *
     * Tests that calling setIntensity on the ViewModel correctly updates the
     * VibrationEngine's intensity for all valid values in [0.0, 1.0].
     */
    @Test
    fun testSetIntensityDelegatesToEngineForAllValidValues() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        val viewModel = GamepadViewModel(engine, this)

        // Test boundary values and representative samples
        val intensities = listOf(
            0.0f,
            0.1f,
            0.2f,
            0.25f,
            0.3f,
            0.4f,
            0.5f,
            0.6f,
            0.7f,
            0.75f,
            0.8f,
            0.9f,
            1.0f
        )

        for (intensity in intensities) {
            viewModel.setIntensity(intensity)
            advanceTimeBy(1) // Allow async operation to complete

            assertEquals(
                intensity,
                viewModel.vibrationState.value.intensity,
                "ViewModel should update intensity to $intensity"
            )

            assertEquals(
                intensity,
                engine.vibrationState.value.intensity,
                "Engine should have intensity $intensity"
            )
        }
    }

    /**
     * Property: setIntensity rejects invalid values below 0.0
     *
     * Tests that intensity values below 0.0 are rejected with an exception.
     */
    @Test
    fun testSetIntensityRejectsValuesBelowZero() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        val viewModel = GamepadViewModel(engine, this)

        val invalidIntensities = listOf(-0.1f, -0.5f, -1.0f, -10.0f)

        for (intensity in invalidIntensities) {
            assertFailsWith<IllegalArgumentException>(
                "setIntensity should reject intensity $intensity"
            ) {
                viewModel.setIntensity(intensity)
            }
        }
    }

    /**
     * Property: setIntensity rejects invalid values above 1.0
     *
     * Tests that intensity values above 1.0 are rejected with an exception.
     */
    @Test
    fun testSetIntensityRejectsValuesAboveOne() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        val viewModel = GamepadViewModel(engine, this)

        val invalidIntensities = listOf(1.1f, 1.5f, 2.0f, 10.0f)

        for (intensity in invalidIntensities) {
            assertFailsWith<IllegalArgumentException>(
                "setIntensity should reject intensity $intensity"
            ) {
                viewModel.setIntensity(intensity)
            }
        }
    }

    /**
     * Property: Intensity updates work during active vibration
     *
     * Tests that intensity can be changed while vibration is actively running,
     * and the new intensity is applied to subsequent frames.
     */
    @Test
    fun testIntensityUpdatesDuringActiveVibration() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        val viewModel = GamepadViewModel(engine, this)

        viewModel.setVibrationEnabled(true)
        advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 2)

        val intensities = listOf(0.2f, 0.5f, 0.8f, 1.0f, 0.3f)

        for (intensity in intensities) {
            viewModel.setIntensity(intensity)
            advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 2)

            assertEquals(intensity, viewModel.vibrationState.value.intensity, "Intensity should update to $intensity")
            assertEquals(true, viewModel.vibrationState.value.enabled, "Vibration should remain enabled")
        }

        // Clean up
        viewModel.setVibrationEnabled(false)
        advanceTimeBy(1)
    }

    /**
     * Property: Intensity updates work when vibration is disabled
     *
     * Tests that intensity can be changed when vibration is disabled, and the
     * new intensity is preserved for when vibration is enabled.
     */
    @Test
    fun testIntensityUpdatesWhenDisabled() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        val viewModel = GamepadViewModel(engine, this)

        // Ensure vibration is disabled
        viewModel.setVibrationEnabled(false)
        advanceTimeBy(1)

        val intensities = listOf(0.3f, 0.6f, 0.9f)

        for (intensity in intensities) {
            viewModel.setIntensity(intensity)
            advanceTimeBy(1)

            assertEquals(
                intensity,
                viewModel.vibrationState.value.intensity,
                "Intensity should be set to $intensity when disabled"
            )

            // Enable vibration and verify intensity is active
            viewModel.setVibrationEnabled(true)
            advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 2)

            assertEquals(
                intensity,
                viewModel.vibrationState.value.intensity,
                "Intensity $intensity should be active after enabling"
            )

            // Disable for next iteration
            viewModel.setVibrationEnabled(false)
            advanceTimeBy(1)
        }
    }

    /**
     * Property: Intensity updates preserve other VibrationState fields
     *
     * Tests that when intensity is updated, other fields in VibrationState
     * (enabled, activePattern) are not modified.
     */
    @Test
    fun testIntensityUpdatesPreserveOtherFields() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        val viewModel = GamepadViewModel(engine, this)

        val patterns = listOf(
            VibrationPattern.Constant,
            VibrationPattern.Pulse(frequencyHz = 2.0f),
            VibrationPattern.Wave(frequencyHz = 1.0f)
        )

        for (pattern in patterns) {
            // Set pattern and enable vibration
            viewModel.setPattern(pattern)
            viewModel.setVibrationEnabled(true)
            advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 2)

            val intensities = listOf(0.3f, 0.7f, 1.0f)

            for (intensity in intensities) {
                viewModel.setIntensity(intensity)
                advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 2)

                val state = viewModel.vibrationState.value
                assertEquals(
                    intensity,
                    state.intensity,
                    "Intensity should be $intensity"
                )
                assertEquals(
                    pattern,
                    state.activePattern,
                    "Active pattern should remain ${pattern::class.simpleName}"
                )
                assertEquals(
                    true,
                    state.enabled,
                    "Enabled state should remain true"
                )
            }

            // Disable for next pattern
            viewModel.setVibrationEnabled(false)
            advanceTimeBy(1)
        }
    }

    /**
     * Property: Multiple rapid intensity changes are all applied
     *
     * Tests that even rapid intensity changes are correctly propagated to the
     * VibrationEngine.
     */
    @Test
    fun testRapidIntensityChanges() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        val viewModel = GamepadViewModel(engine, this)

        val intensities = listOf(0.1f, 0.3f, 0.5f, 0.7f, 0.9f, 0.4f, 0.6f, 0.2f)

        for (intensity in intensities) {
            viewModel.setIntensity(intensity)
            advanceTimeBy(1) // Minimal delay

            assertEquals(
                intensity,
                viewModel.vibrationState.value.intensity,
                "Intensity should update to $intensity even with rapid changes"
            )
        }
    }

    /**
     * Property: Intensity is consistent between ViewModel and Engine
     *
     * Tests that the intensity in the ViewModel always matches the intensity
     * in the VibrationEngine.
     */
    @Test
    fun testIntensityConsistencyBetweenViewModelAndEngine() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        val viewModel = GamepadViewModel(engine, this)

        val intensities = listOf(0.0f, 0.25f, 0.5f, 0.75f, 1.0f)

        for (intensity in intensities) {
            viewModel.setIntensity(intensity)
            advanceTimeBy(1)

            assertEquals(
                engine.vibrationState.value.intensity,
                viewModel.vibrationState.value.intensity,
                "ViewModel and Engine intensities should be consistent at $intensity"
            )
        }
    }

    /**
     * Property: Intensity updates work across pattern changes
     *
     * Tests that intensity is preserved when patterns are changed, and new
     * intensity values can be set after pattern changes.
     */
    @Test
    fun testIntensityUpdatesAcrossPatternChanges() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        val viewModel = GamepadViewModel(engine, this)

        val patterns = listOf(
            VibrationPattern.Constant,
            VibrationPattern.Pulse(frequencyHz = 2.0f),
            VibrationPattern.Wave(frequencyHz = 1.0f)
        )

        val intensities = listOf(0.3f, 0.6f, 0.9f)

        for ((patternIndex, pattern) in patterns.withIndex()) {
            val intensity = intensities[patternIndex]

            // Set intensity
            viewModel.setIntensity(intensity)
            advanceTimeBy(1)

            // Change pattern
            viewModel.setPattern(pattern)
            advanceTimeBy(1)

            // Verify intensity is preserved
            assertEquals(
                intensity,
                viewModel.vibrationState.value.intensity,
                "Intensity $intensity should be preserved after pattern change to ${pattern::class.simpleName}"
            )

            // Set new intensity after pattern change
            val newIntensity = intensity + 0.05f
            if (newIntensity <= 1.0f) {
                viewModel.setIntensity(newIntensity)
                advanceTimeBy(1)

                assertEquals(
                    newIntensity,
                    viewModel.vibrationState.value.intensity,
                    "New intensity $newIntensity should be set after pattern change"
                )
            }
        }
    }

    /**
     * Property: Intensity updates work across enable/disable cycles
     *
     * Tests that intensity updates work correctly through multiple
     * enable/disable cycles.
     */
    @Test
    fun testIntensityUpdatesAcrossEnableDisableCycles() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        val viewModel = GamepadViewModel(engine, this)

        val intensities = listOf(0.2f, 0.5f, 0.8f)

        for (intensity in intensities) {
            // Set intensity while disabled
            viewModel.setVibrationEnabled(false)
            advanceTimeBy(1)
            viewModel.setIntensity(intensity)
            advanceTimeBy(1)

            assertEquals(
                intensity,
                viewModel.vibrationState.value.intensity,
                "Intensity should be $intensity when disabled"
            )

            // Enable and verify intensity persists
            viewModel.setVibrationEnabled(true)
            advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 2)

            assertEquals(
                intensity,
                viewModel.vibrationState.value.intensity,
                "Intensity should remain $intensity after enabling"
            )

            // Disable again
            viewModel.setVibrationEnabled(false)
            advanceTimeBy(1)

            assertEquals(
                intensity,
                viewModel.vibrationState.value.intensity,
                "Intensity should remain $intensity after disabling"
            )
        }
    }

    /**
     * Property: Extreme valid intensity values (0.0 and 1.0) work correctly
     *
     * Tests that the boundary values 0.0 and 1.0 are handled correctly.
     */
    @Test
    fun testExtremeValidIntensityValues() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        val viewModel = GamepadViewModel(engine, this)

        // Test minimum intensity (0.0)
        viewModel.setIntensity(0.0f)
        advanceTimeBy(1)
        assertEquals(
            0.0f,
            viewModel.vibrationState.value.intensity,
            "Minimum intensity 0.0 should be accepted"
        )

        // Test maximum intensity (1.0)
        viewModel.setIntensity(1.0f)
        advanceTimeBy(1)
        assertEquals(
            1.0f,
            viewModel.vibrationState.value.intensity,
            "Maximum intensity 1.0 should be accepted"
        )

        // Test alternating between extremes
        for (i in 1..3) {
            viewModel.setIntensity(0.0f)
            advanceTimeBy(1)
            assertEquals(0.0f, viewModel.vibrationState.value.intensity)

            viewModel.setIntensity(1.0f)
            advanceTimeBy(1)
            assertEquals(1.0f, viewModel.vibrationState.value.intensity)
        }
    }
}
