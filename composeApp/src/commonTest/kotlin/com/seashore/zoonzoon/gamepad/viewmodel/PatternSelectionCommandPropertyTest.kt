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
 * Property-based tests for pattern selection command.
 *
 * **Validates: Requirement 3.1**
 *
 * Property 5: Pattern Selection Command
 * For any vibration pattern and intensity value, when the user selects that pattern,
 * the ViewModel SHALL call SetPattern on the VibrationEngine with the selected pattern
 * and current intensity.
 *
 * This test validates that the ViewModel correctly delegates pattern selection to the
 * VibrationEngine, ensuring the selected pattern is activated.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PatternSelectionCommandPropertyTest {

    /**
     * Property: setPattern delegates to VibrationEngine for all pattern types
     *
     * Tests that calling setPattern on the ViewModel correctly updates the
     * VibrationEngine's active pattern for all supported pattern types.
     */
    @Test
    fun testSetPatternDelegatesToEngineForAllPatterns() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        val viewModel = GamepadViewModel(engine, this)

        val patterns = listOf(
            VibrationPattern.Constant,
            VibrationPattern.Pulse(frequencyHz = 1.0f),
            VibrationPattern.Pulse(frequencyHz = 2.0f),
            VibrationPattern.Pulse(frequencyHz = 5.0f),
            VibrationPattern.Wave(frequencyHz = 0.5f),
            VibrationPattern.Wave(frequencyHz = 1.0f),
            VibrationPattern.Wave(frequencyHz = 2.0f),
            VibrationPattern.Custom(
                listOf(VibrationPattern.Custom.Frame(0.5f, 0.5f, 100L)),
                loop = true
            ),
            VibrationPattern.Custom(
                listOf(
                    VibrationPattern.Custom.Frame(0.8f, 0.2f, 50L),
                    VibrationPattern.Custom.Frame(0.2f, 0.8f, 50L)
                ),
                loop = false
            )
        )

        for (pattern in patterns) {
            viewModel.setPattern(pattern)
            advanceTimeBy(1) // Allow async operation to complete

            assertEquals(
                pattern,
                viewModel.vibrationState.value.activePattern,
                "ViewModel should update active pattern to: ${pattern::class.simpleName}"
            )

            assertEquals(
                pattern,
                engine.vibrationState.value.activePattern,
                "Engine should have active pattern: ${pattern::class.simpleName}"
            )
        }
    }

    /**
     * Property: Pattern selection preserves intensity
     *
     * Tests that when a pattern is selected, the current intensity value is
     * preserved (not reset or modified).
     */
    @Test
    fun testPatternSelectionPreservesIntensity() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        val viewModel = GamepadViewModel(engine, this)

        val intensities = listOf(0.0f, 0.25f, 0.5f, 0.75f, 1.0f)
        val patterns = listOf(
            VibrationPattern.Constant,
            VibrationPattern.Pulse(frequencyHz = 2.0f),
            VibrationPattern.Wave(frequencyHz = 1.0f)
        )

        for (intensity in intensities) {
            // Set intensity
            viewModel.setIntensity(intensity)
            advanceTimeBy(1)

            for (pattern in patterns) {
                // Select pattern
                viewModel.setPattern(pattern)
                advanceTimeBy(1)

                // Verify intensity is preserved
                assertEquals(
                    intensity,
                    viewModel.vibrationState.value.intensity,
                    "Intensity should be preserved at $intensity after selecting ${pattern::class.simpleName}"
                )

                assertEquals(
                    intensity,
                    engine.vibrationState.value.intensity,
                    "Engine intensity should be $intensity after selecting ${pattern::class.simpleName}"
                )
            }
        }
    }

    /**
     * Property: Pattern selection works during active vibration
     *
     * Tests that patterns can be changed while vibration is actively running,
     * and the new pattern takes effect immediately.
     */
    @Test
    fun testPatternSelectionDuringActiveVibration() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        val viewModel = GamepadViewModel(engine, this)

        val patterns = listOf(
            VibrationPattern.Constant,
            VibrationPattern.Pulse(frequencyHz = 2.0f),
            VibrationPattern.Wave(frequencyHz = 1.0f),
            VibrationPattern.Custom(listOf(VibrationPattern.Custom.Frame(0.7f, 0.3f, 100L)), loop = true)
        )

        viewModel.setVibrationEnabled(true)
        advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 2)

        for (pattern in patterns) {
            viewModel.setPattern(pattern)
            advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 2)

            assertEquals(pattern, viewModel.vibrationState.value.activePattern, "Pattern should change to ${pattern::class.simpleName}")
            assertEquals(true, viewModel.vibrationState.value.enabled, "Vibration should remain enabled")
        }

        // Clean up
        viewModel.setVibrationEnabled(false)
        advanceTimeBy(1)
    }

    /**
     * Property: Pattern selection works when vibration is disabled
     *
     * Tests that patterns can be selected when vibration is disabled, and the
     * selection is preserved for when vibration is enabled.
     */
    @Test
    fun testPatternSelectionWhenDisabled() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        val viewModel = GamepadViewModel(engine, this)

        val patterns = listOf(
            VibrationPattern.Constant,
            VibrationPattern.Pulse(frequencyHz = 3.0f),
            VibrationPattern.Wave(frequencyHz = 1.5f)
        )

        for (pattern in patterns) {
            // Ensure vibration is disabled
            viewModel.setVibrationEnabled(false)
            advanceTimeBy(1)

            // Select pattern while disabled
            viewModel.setPattern(pattern)
            advanceTimeBy(1)

            assertEquals(
                pattern,
                viewModel.vibrationState.value.activePattern,
                "Pattern should be set to ${pattern::class.simpleName} even when disabled"
            )

            // Enable vibration and verify pattern is active
            viewModel.setVibrationEnabled(true)
            advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 2)

            assertEquals(
                pattern,
                viewModel.vibrationState.value.activePattern,
                "Pattern ${pattern::class.simpleName} should be active after enabling"
            )

            assertEquals(
                true,
                viewModel.vibrationState.value.enabled,
                "Vibration should be enabled"
            )

            // Disable for next iteration
            viewModel.setVibrationEnabled(false)
            advanceTimeBy(1)
        }
    }

    /**
     * Property: Multiple rapid pattern selections are all applied
     *
     * Tests that even rapid pattern changes are correctly propagated to the
     * VibrationEngine.
     */
    @Test
    fun testRapidPatternSelections() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        val viewModel = GamepadViewModel(engine, this)

        val patterns = listOf(
            VibrationPattern.Constant,
            VibrationPattern.Pulse(frequencyHz = 1.0f),
            VibrationPattern.Wave(frequencyHz = 0.5f),
            VibrationPattern.Pulse(frequencyHz = 3.0f),
            VibrationPattern.Wave(frequencyHz = 2.0f),
            VibrationPattern.Constant
        )

        for (pattern in patterns) {
            viewModel.setPattern(pattern)
            advanceTimeBy(1) // Minimal delay

            assertEquals(
                pattern,
                viewModel.vibrationState.value.activePattern,
                "Pattern should update to ${pattern::class.simpleName} even with rapid changes"
            )
        }
    }

    /**
     * Property: Pattern selection is consistent between ViewModel and Engine
     *
     * Tests that the active pattern in the ViewModel always matches the active
     * pattern in the VibrationEngine.
     */
    @Test
    fun testPatternSelectionConsistencyBetweenViewModelAndEngine() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        val viewModel = GamepadViewModel(engine, this)

        val patterns = listOf(
            VibrationPattern.Constant,
            VibrationPattern.Pulse(frequencyHz = 2.0f),
            VibrationPattern.Wave(frequencyHz = 1.0f),
            VibrationPattern.Custom(
                listOf(
                    VibrationPattern.Custom.Frame(0.5f, 0.5f, 100L),
                    VibrationPattern.Custom.Frame(0.8f, 0.2f, 100L)
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
                "ViewModel and Engine patterns should be consistent for: ${pattern::class.simpleName}"
            )
        }
    }

    /**
     * Property: Custom patterns with various configurations are supported
     *
     * Tests that custom patterns with different frame counts, durations, and
     * loop settings are correctly handled by the ViewModel.
     */
    @Test
    fun testCustomPatternVariations() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        val viewModel = GamepadViewModel(engine, this)

        val customPatterns = listOf(
            // Single frame, looping
            VibrationPattern.Custom(
                listOf(VibrationPattern.Custom.Frame(0.5f, 0.5f, 100L)),
                loop = true
            ),
            // Single frame, non-looping
            VibrationPattern.Custom(
                listOf(VibrationPattern.Custom.Frame(0.8f, 0.2f, 200L)),
                loop = false
            ),
            // Multiple frames, looping
            VibrationPattern.Custom(
                listOf(
                    VibrationPattern.Custom.Frame(1.0f, 0.0f, 50L),
                    VibrationPattern.Custom.Frame(0.0f, 1.0f, 50L),
                    VibrationPattern.Custom.Frame(0.5f, 0.5f, 50L)
                ),
                loop = true
            ),
            // Multiple frames, non-looping
            VibrationPattern.Custom(
                listOf(
                    VibrationPattern.Custom.Frame(0.3f, 0.7f, 100L),
                    VibrationPattern.Custom.Frame(0.7f, 0.3f, 100L)
                ),
                loop = false
            ),
            // Many frames
            VibrationPattern.Custom(
                (0..9).map { i ->
                    VibrationPattern.Custom.Frame(
                        i / 10.0f,
                        (10 - i) / 10.0f,
                        50L
                    )
                },
                loop = true
            )
        )

        for (pattern in customPatterns) {
            viewModel.setPattern(pattern)
            advanceTimeBy(1)

            assertEquals(
                pattern,
                viewModel.vibrationState.value.activePattern,
                "Custom pattern should be set correctly"
            )
        }
    }
}
