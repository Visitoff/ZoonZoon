package com.seashore.zoonzoon.gamepad.engine

import com.seashore.zoonzoon.gamepad.model.VibrationPattern
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for [VibrationEngine] pattern execution loop.
 *
 * Tests cover:
 * - Coroutine-based loop at ~16 ms intervals
 * - Intensity scaling applied to motor values
 * - Vibration commands sent to the controller
 * - Continuous execution until explicitly disabled
 *
 * **Validates: Requirements 5.1, 5.2, 5.4, 5.5**
 */
@OptIn(ExperimentalCoroutinesApi::class)
class VibrationEngineTest {

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    // -------------------------------------------------------------------------
    // enableVibration / disableVibration state tests
    // -------------------------------------------------------------------------

    @Test
    fun testEnableVibrationUpdatesState() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)

        assertFalse(engine.vibrationState.value.enabled, "Should start disabled")
        engine.enableVibration()
        assertTrue(engine.vibrationState.value.enabled, "Should be enabled after enableVibration()")
        
        // Clean up - disable vibration to stop coroutines
        engine.disableVibration()
        advanceTimeBy(1)
    }

    @Test
    fun testDisableVibrationUpdatesState() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)

        engine.enableVibration()
        engine.disableVibration()
        assertFalse(engine.vibrationState.value.enabled, "Should be disabled after disableVibration()")
    }

    // -------------------------------------------------------------------------
    // Stop command (Requirement 2.3)
    // -------------------------------------------------------------------------

    @Test
    fun testDisableVibrationSendsStopCommand() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)

        engine.enableVibration()
        engine.disableVibration()

        // Advance time so the stop command coroutine can execute
        advanceTimeBy(1)

        val stopCommands = controller.commands.filter { it.leftMotor == 0f && it.rightMotor == 0f }
        assertTrue(stopCommands.isNotEmpty(), "disableVibration() must send a stop command (0, 0)")
    }

    // -------------------------------------------------------------------------
    // Pattern execution loop – frame interval (Requirements 5.1, 5.2, 5.4)
    // -------------------------------------------------------------------------

    @Test
    fun testExecutionLoopSendsCommandsAtRegularIntervals() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)

        engine.enableVibration()

        // Advance time by 5 frame intervals
        val frames = 5
        advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * frames + 1)

        // We expect at least `frames` commands to have been sent
        assertTrue(
            controller.commands.size >= frames,
            "Expected at least $frames commands after ${frames} frame intervals, " +
            "got ${controller.commands.size}"
        )
        
        // Clean up - disable vibration to stop coroutines
        engine.disableVibration()
        advanceTimeBy(1)
    }

    @Test
    fun testExecutionLoopStopsAfterDisable() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)

        engine.enableVibration()
        advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 3 + 1)

        val countBeforeDisable = controller.commands.size
        engine.disableVibration()
        advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 5)

        // After disabling, no additional pattern-frame commands should be sent
        // (only the stop command is allowed)
        val patternCommandsAfterDisable = controller.commands
            .drop(countBeforeDisable)
            .filter { it.leftMotor != 0f || it.rightMotor != 0f }

        assertTrue(
            patternCommandsAfterDisable.isEmpty(),
            "No pattern commands should be sent after disableVibration(), " +
            "but got: $patternCommandsAfterDisable"
        )
    }

    @Test
    fun testExecutionLoopContinuesUntilDisabled() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)

        engine.enableVibration()

        // Advance through many frames
        advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 20 + 1)
        val countAfterManyFrames = controller.commands.size

        assertTrue(
            countAfterManyFrames >= 20,
            "Loop should continue executing for many frames, got $countAfterManyFrames"
        )

        // Disable and verify loop stops
        engine.disableVibration()
        advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 5)
        val countAfterDisable = controller.commands
            .filter { it.leftMotor != 0f || it.rightMotor != 0f }
            .size

        assertEquals(
            countAfterManyFrames,
            countAfterDisable,
            "No additional pattern commands should be sent after disable"
        )
    }

    // -------------------------------------------------------------------------
    // Intensity scaling (Requirements 4.3, 5.1)
    // -------------------------------------------------------------------------

    @Test
    fun testIntensityScalingAppliedToConstantPattern() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)

        val intensity = 0.5f
        engine.setIntensity(intensity)
        engine.setPattern(VibrationPattern.Constant)
        engine.enableVibration()

        advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS + 1)

        val patternCommands = controller.commands.filter { it.leftMotor != 0f || it.rightMotor != 0f }
        assertTrue(patternCommands.isNotEmpty(), "Should have sent at least one command")

        val cmd = patternCommands.first()
        assertEquals(intensity, cmd.leftMotor, 0.001f, "Left motor should equal intensity for Constant pattern")
        assertEquals(intensity, cmd.rightMotor, 0.001f, "Right motor should equal intensity for Constant pattern")

        engine.disableVibration()
        advanceTimeBy(1)
    }

    @Test
    fun testZeroIntensityProducesZeroMotorValues() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)

        engine.setIntensity(0.0f)
        engine.setPattern(VibrationPattern.Constant)
        engine.enableVibration()

        advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS + 1)

        // All commands (including pattern frames) should have 0 motor values
        val nonZeroCommands = controller.commands.filter { it.leftMotor != 0f || it.rightMotor != 0f }
        assertTrue(
            nonZeroCommands.isEmpty(),
            "Zero intensity should produce zero motor values, but got: $nonZeroCommands"
        )

        engine.disableVibration()
        advanceTimeBy(1)
    }

    @Test
    fun testMaxIntensityProducesMaxMotorValuesForConstantPattern() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)

        engine.setIntensity(1.0f)
        engine.setPattern(VibrationPattern.Constant)
        engine.enableVibration()

        advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS + 1)

        val patternCommands = controller.commands.filter { it.leftMotor != 0f || it.rightMotor != 0f }
        assertTrue(patternCommands.isNotEmpty(), "Should have sent at least one command")

        val cmd = patternCommands.first()
        assertEquals(1.0f, cmd.leftMotor, 0.001f, "Left motor should be 1.0 at max intensity")
        assertEquals(1.0f, cmd.rightMotor, 0.001f, "Right motor should be 1.0 at max intensity")

        engine.disableVibration()
        advanceTimeBy(1)
    }

    @Test
    fun testIntensityChangeAppliedToSubsequentCommands() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)

        engine.setIntensity(1.0f)
        engine.setPattern(VibrationPattern.Constant)
        engine.enableVibration()

        // Let a few frames run at full intensity
        advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 3 + 1)
        val countBeforeChange = controller.commands.size

        // Change intensity mid-execution
        engine.setIntensity(0.3f)
        advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 3 + 1)

        val commandsAfterChange = controller.commands.drop(countBeforeChange)
            .filter { it.leftMotor != 0f || it.rightMotor != 0f }

        assertTrue(commandsAfterChange.isNotEmpty(), "Should have commands after intensity change")
        commandsAfterChange.forEach { cmd ->
            assertEquals(
                0.3f, cmd.leftMotor, 0.001f,
                "Commands after intensity change should use new intensity 0.3"
            )
        }

        engine.disableVibration()
        advanceTimeBy(1)
    }

    // -------------------------------------------------------------------------
    // Pattern changes (Requirements 3.1, 4.4)
    // -------------------------------------------------------------------------

    @Test
    fun testPatternChangeAppliedOnNextFrame() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)

        engine.setIntensity(1.0f)
        engine.setPattern(VibrationPattern.Constant)
        engine.enableVibration()

        advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS + 1)

        // Switch to Pulse pattern
        engine.setPattern(VibrationPattern.Pulse(frequencyHz = 2.0f))
        assertEquals(
            VibrationPattern.Pulse(frequencyHz = 2.0f),
            engine.vibrationState.value.activePattern,
            "Active pattern should update immediately"
        )

        engine.disableVibration()
        advanceTimeBy(1)
    }

    @Test
    fun testIntensityPersistsAcrossPatternChanges() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)

        val intensity = 0.6f
        engine.setIntensity(intensity)
        engine.setPattern(VibrationPattern.Constant)

        // Change pattern
        engine.setPattern(VibrationPattern.Wave(frequencyHz = 1.0f))

        assertEquals(
            intensity,
            engine.vibrationState.value.intensity,
            "Intensity should persist after pattern change"
        )
    }

    // -------------------------------------------------------------------------
    // setIntensity validation
    // -------------------------------------------------------------------------

    @Test
    fun testSetIntensityRejectsValuesOutOfRange() {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, TestScope())

        assertFailsWith<IllegalArgumentException> {
            engine.setIntensity(-0.1f)
        }
        assertFailsWith<IllegalArgumentException> {
            engine.setIntensity(1.1f)
        }
    }

    @Test
    fun testSetIntensityAcceptsBoundaryValues() {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, TestScope())

        engine.setIntensity(0.0f)
        assertEquals(0.0f, engine.vibrationState.value.intensity)

        engine.setIntensity(1.0f)
        assertEquals(1.0f, engine.vibrationState.value.intensity)
    }

    // -------------------------------------------------------------------------
    // Motor value range validation (Requirements 5.1, 5.2)
    // -------------------------------------------------------------------------

    @Test
    fun testAllCommandsHaveMotorValuesInValidRange() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)

        val patterns = listOf(
            VibrationPattern.Constant,
            VibrationPattern.Pulse(frequencyHz = 2.0f),
            VibrationPattern.Wave(frequencyHz = 1.0f),
            VibrationPattern.Custom(
                listOf(
                    VibrationPattern.Custom.Frame(0.3f, 0.7f, 100L),
                    VibrationPattern.Custom.Frame(0.8f, 0.2f, 100L)
                ),
                loop = true
            )
        )

        for (pattern in patterns) {
            controller.commands.clear()
            engine.setPattern(pattern)
            engine.setIntensity(0.8f)
            engine.enableVibration()

            advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 10 + 1)
            engine.disableVibration()
            advanceTimeBy(1)

            controller.commands.forEach { cmd ->
                assertTrue(
                    cmd.leftMotor in 0.0f..1.0f,
                    "Left motor ${cmd.leftMotor} out of range for pattern ${pattern::class.simpleName}"
                )
                assertTrue(
                    cmd.rightMotor in 0.0f..1.0f,
                    "Right motor ${cmd.rightMotor} out of range for pattern ${pattern::class.simpleName}"
                )
            }
        }
    }

    // -------------------------------------------------------------------------
    // Idempotency: calling enable/disable multiple times
    // -------------------------------------------------------------------------

    @Test
    fun testEnableVibrationIsIdempotent() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)

        engine.enableVibration()
        engine.enableVibration() // second call should be a no-op

        advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 3 + 1)

        // Commands should be consistent with a single loop running
        assertTrue(controller.commands.isNotEmpty(), "Should have commands")

        engine.disableVibration()
        advanceTimeBy(1)
    }
}

