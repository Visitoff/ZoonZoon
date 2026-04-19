package com.seashore.zoonzoon.gamepad.viewmodel

import com.seashore.zoonzoon.gamepad.engine.FakePlatformGamepadController
import com.seashore.zoonzoon.gamepad.engine.VibrationEngine
import com.seashore.zoonzoon.gamepad.model.VibrationPattern
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Property-based tests for pattern selection state update.
 *
 * **Validates: Requirement 3.6**
 *
 * Property 7: Pattern Selection State Update
 * For any pattern selection, the System SHALL update the VibrationState to reflect
 * the newly active pattern.
 *
 * This test validates that when a pattern is selected, the VibrationState exposed
 * by the ViewModel is updated to reflect the new active pattern.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PatternSelectionStateUpdatePropertyTest {

    /**
     * Property: VibrationState updates to reflect pattern selection for all patterns
     *
     * Tests that the VibrationState.activePattern field is updated whenever a
     * pattern is selected.
     */
    @Test
    fun testVibrationStateUpdatesForAllPatterns() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        val viewModel = GamepadViewModel(engine, this)

        val patterns = listOf(
            VibrationPattern.Constant,
            VibrationPattern.Pulse(frequencyHz = 1.0f),
            VibrationPattern.Pulse(frequencyHz = 2.5f),
            VibrationPattern.Pulse(frequencyHz = 5.0f),
            VibrationPattern.Wave(frequencyHz = 0.5f),
            VibrationPattern.Wave(frequencyHz = 1.5f),
            VibrationPattern.Wave(frequencyHz = 3.0f),
            VibrationPattern.Custom(
                listOf(VibrationPattern.Custom.Frame(0.6f, 0.4f, 100L)),
                loop = true
            ),
            VibrationPattern.Custom(
                listOf(
                    VibrationPattern.Custom.Frame(0.9f, 0.1f, 50L),
                    VibrationPattern.Custom.Frame(0.1f, 0.9f, 50L)
                ),
                loop = false
            )
        )

        for (pattern in patterns) {
            viewModel.setPattern(pattern)
            advanceTimeBy(1) // Allow state update to propagate

            assertEquals(
                pattern,
                viewModel.vibrationState.value.activePattern,
                "VibrationState should reflect active pattern: ${pattern::class.simpleName}"
            )
        }
    }

    /**
     * Property: State update occurs immediately after pattern selection
     *
     * Tests that the VibrationState is updated synchronously or near-immediately
     * after a pattern selection command.
     */
    @Test
    fun testStateUpdateOccursImmediately() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        val viewModel = GamepadViewModel(engine, this)

        val patterns = listOf(
            VibrationPattern.Constant,
            VibrationPattern.Pulse(frequencyHz = 2.0f),
            VibrationPattern.Wave(frequencyHz = 1.0f)
        )

        for (pattern in patterns) {
            val previousPattern = viewModel.vibrationState.value.activePattern

            viewModel.setPattern(pattern)
            advanceTimeBy(1) // Minimal delay

            assertEquals(
                pattern,
                viewModel.vibrationState.value.activePattern,
                "State should update immediately from ${previousPattern::class.simpleName} to ${pattern::class.simpleName}"
            )
        }
    }

    /**
     * Property: State update preserves other VibrationState fields
     *
     * Tests that when the active pattern is updated, other fields in VibrationState
     * (enabled, intensity) are not modified.
     */
    @Test
    fun testStateUpdatePreservesOtherFields() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        val viewModel = GamepadViewModel(engine, this)

        val patterns = listOf(
            VibrationPattern.Constant,
            VibrationPattern.Pulse(frequencyHz = 2.0f),
            VibrationPattern.Wave(frequencyHz = 1.0f)
        )

        viewModel.setVibrationEnabled(false)
        viewModel.setIntensity(0.6f)
        advanceTimeBy(1)

        for (pattern in patterns) {
            viewModel.setPattern(pattern)
            advanceTimeBy(1)
            val state = viewModel.vibrationState.value
            assertEquals(pattern, state.activePattern)
            assertEquals(false, state.enabled)
            assertEquals(0.6f, state.intensity)
        }

        viewModel.setVibrationEnabled(true)
        viewModel.setIntensity(0.8f)
        advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 2)

        for (pattern in patterns) {
            viewModel.setPattern(pattern)
            advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 2)
            val state = viewModel.vibrationState.value
            assertEquals(pattern, state.activePattern)
            assertEquals(true, state.enabled)
            assertEquals(0.8f, state.intensity)
        }

        // Clean up
        viewModel.setVibrationEnabled(false)
        advanceTimeBy(1)
    }

    /**
     * Property: State updates work during active vibration
     *
     * Tests that the VibrationState correctly reflects pattern changes even
     * when vibration is actively running.
     */
    @Test
    fun testStateUpdatesDuringActiveVibration() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        val viewModel = GamepadViewModel(engine, this)

        viewModel.setVibrationEnabled(true)
        advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 2)

        val patterns = listOf(
            VibrationPattern.Constant,
            VibrationPattern.Pulse(frequencyHz = 3.0f),
            VibrationPattern.Wave(frequencyHz = 1.5f),
            VibrationPattern.Custom(listOf(VibrationPattern.Custom.Frame(0.7f, 0.3f, 100L)), loop = true)
        )

        for (pattern in patterns) {
            viewModel.setPattern(pattern)
            advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 2)
            assertEquals(pattern, viewModel.vibrationState.value.activePattern)
            assertEquals(true, viewModel.vibrationState.value.enabled)
        }

        // Clean up
        viewModel.setVibrationEnabled(false)
        advanceTimeBy(1)
    }

    /**
     * Property: State updates work when vibration is disabled
     *
     * Tests that the VibrationState correctly reflects pattern changes even
     * when vibration is disabled.
     */
    @Test
    fun testStateUpdatesWhenDisabled() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        val viewModel = GamepadViewModel(engine, this)

        // Ensure vibration is disabled
        viewModel.setVibrationEnabled(false)
        advanceTimeBy(1)

        val patterns = listOf(
            VibrationPattern.Constant,
            VibrationPattern.Pulse(frequencyHz = 2.0f),
            VibrationPattern.Wave(frequencyHz = 1.0f)
        )

        for (pattern in patterns) {
            viewModel.setPattern(pattern)
            advanceTimeBy(1)

            assertEquals(
                pattern,
                viewModel.vibrationState.value.activePattern,
                "VibrationState should reflect ${pattern::class.simpleName} when disabled"
            )

            assertEquals(
                false,
                viewModel.vibrationState.value.enabled,
                "Vibration should remain disabled"
            )
        }
    }

    /**
     * Property: Multiple rapid pattern changes all update state
     *
     * Tests that even rapid pattern selections result in correct state updates.
     */
    @Test
    fun testRapidPatternChangesUpdateState() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        val viewModel = GamepadViewModel(engine, this)

        val patterns = listOf(
            VibrationPattern.Constant,
            VibrationPattern.Pulse(frequencyHz = 1.0f),
            VibrationPattern.Wave(frequencyHz = 0.5f),
            VibrationPattern.Pulse(frequencyHz = 3.0f),
            VibrationPattern.Wave(frequencyHz = 2.0f),
            VibrationPattern.Constant,
            VibrationPattern.Custom(
                listOf(VibrationPattern.Custom.Frame(0.5f, 0.5f, 100L)),
                loop = true
            )
        )

        for (pattern in patterns) {
            viewModel.setPattern(pattern)
            advanceTimeBy(1) // Minimal delay

            assertEquals(
                pattern,
                viewModel.vibrationState.value.activePattern,
                "VibrationState should update to ${pattern::class.simpleName} even with rapid changes"
            )
        }
    }

    /**
     * Property: State is consistent across ViewModel and Engine
     *
     * Tests that the active pattern in the ViewModel's VibrationState always
     * matches the active pattern in the Engine's VibrationState.
     */
    @Test
    fun testStateConsistencyBetweenViewModelAndEngine() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        val viewModel = GamepadViewModel(engine, this)

        val patterns = listOf(
            VibrationPattern.Constant,
            VibrationPattern.Pulse(frequencyHz = 2.0f),
            VibrationPattern.Wave(frequencyHz = 1.0f),
            VibrationPattern.Custom(
                listOf(
                    VibrationPattern.Custom.Frame(0.4f, 0.6f, 100L),
                    VibrationPattern.Custom.Frame(0.6f, 0.4f, 100L)
                ),
                loop = true
            )
        )

        for (pattern in patterns) {
            viewModel.setPattern(pattern)
            advanceTimeBy(1)

            assertEquals(
                engine.vibrationState.value.activePattern,
                viewModel.vibrationState.value.activePattern,
                "ViewModel and Engine active patterns should be consistent for: ${pattern::class.simpleName}"
            )
        }
    }

    /**
     * Property: State updates work across enable/disable cycles
     *
     * Tests that pattern state updates work correctly through multiple
     * enable/disable cycles.
     */
    @Test
    fun testStateUpdatesAcrossEnableDisableCycles() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        val viewModel = GamepadViewModel(engine, this)

        val patterns = listOf(
            VibrationPattern.Constant,
            VibrationPattern.Pulse(frequencyHz = 2.0f),
            VibrationPattern.Wave(frequencyHz = 1.0f)
        )

        for (pattern in patterns) {
            // Set pattern while disabled
            viewModel.setVibrationEnabled(false)
            advanceTimeBy(1)
            viewModel.setPattern(pattern)
            advanceTimeBy(1)

            assertEquals(
                pattern,
                viewModel.vibrationState.value.activePattern,
                "Pattern should be ${pattern::class.simpleName} when disabled"
            )

            // Enable and verify pattern persists
            viewModel.setVibrationEnabled(true)
            advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 2)

            assertEquals(
                pattern,
                viewModel.vibrationState.value.activePattern,
                "Pattern should remain ${pattern::class.simpleName} after enabling"
            )

            // Disable again
            viewModel.setVibrationEnabled(false)
            advanceTimeBy(1)

            assertEquals(
                pattern,
                viewModel.vibrationState.value.activePattern,
                "Pattern should remain ${pattern::class.simpleName} after disabling"
            )
        }
    }
}
