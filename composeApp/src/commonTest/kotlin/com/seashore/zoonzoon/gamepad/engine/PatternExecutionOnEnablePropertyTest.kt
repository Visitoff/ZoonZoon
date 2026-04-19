package com.seashore.zoonzoon.gamepad.engine

import com.seashore.zoonzoon.gamepad.model.VibrationPattern
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Property-based tests for pattern execution on enable.
 *
 * **Validates: Requirement 2.4**
 *
 * Property 4: Pattern Execution on Enable
 * For any currently selected VibrationPattern, when vibration is enabled,
 * the System SHALL execute that pattern.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PatternExecutionOnEnablePropertyTest {

    private class FakeGamepadController : GamepadController {
        data class Command(val leftMotor: Float, val rightMotor: Float)
        val commands = mutableListOf<Command>()
        override suspend fun sendVibrationCommand(leftMotor: Float, rightMotor: Float): Result<Unit> {
            commands.add(Command(leftMotor, rightMotor))
            return Result.success(Unit)
        }
        fun reset() = commands.clear()
    }

    /**
     * Property: Enabling vibration must cause the currently selected pattern to execute.
     * Verified by checking that commands are sent after enableVibration().
     */
    @Test
    fun testEnablingVibrationExecutesSelectedPattern() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)

        val patterns = listOf(
            VibrationPattern.Constant,
            VibrationPattern.Pulse(frequencyHz = 2.0f),
            VibrationPattern.Wave(frequencyHz = 1.0f),
            VibrationPattern.Custom(
                listOf(VibrationPattern.Custom.Frame(0.5f, 0.5f, 100L)),
                loop = true
            )
        )

        for (pattern in patterns) {
            controller.reset()
            engine.setPattern(pattern)
            engine.setIntensity(0.8f)

            // No commands before enabling
            advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 2)
            assertTrue(
                controller.commands.isEmpty(),
                "No commands should be sent before enable for ${pattern::class.simpleName}"
            )

            // Enable — pattern must start executing
            engine.enableVibration()
            advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 3)

            assertTrue(
                controller.commands.isNotEmpty(),
                "Commands must be sent after enableVibration() for ${pattern::class.simpleName}"
            )

            engine.disableVibration()
            advanceTimeBy(1)
        }
    }

    /**
     * Property: The pattern that was selected before enabling is the one that executes.
     * For Constant pattern at full intensity, motor values must equal intensity.
     */
    @Test
    fun testSelectedPatternIsExecutedNotADifferentOne() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)

        // Constant pattern at full intensity → every command must have left == right == 1.0
        engine.setPattern(VibrationPattern.Constant)
        engine.setIntensity(1.0f)
        engine.enableVibration()
        advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 3)

        val patternCommands = controller.commands.filter { it.leftMotor != 0f || it.rightMotor != 0f }
        assertTrue(patternCommands.isNotEmpty(), "Should have pattern commands")
        patternCommands.forEach { cmd ->
            assertTrue(
                cmd.leftMotor == 1.0f && cmd.rightMotor == 1.0f,
                "Constant pattern at 1.0 intensity must produce (1.0, 1.0), got (${ cmd.leftMotor}, ${cmd.rightMotor})"
            )
        }

        engine.disableVibration()
        advanceTimeBy(1)
    }

    /**
     * Property: Pattern execution starts for all intensity values including 0.
     */
    @Test
    fun testPatternExecutesForAllIntensities() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)

        val intensities = listOf(0.0f, 0.5f, 1.0f)

        for (intensity in intensities) {
            controller.reset()
            engine.setPattern(VibrationPattern.Constant)
            engine.setIntensity(intensity)
            engine.enableVibration()
            advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 3)

            assertTrue(
                controller.commands.isNotEmpty(),
                "Commands must be sent for intensity=$intensity"
            )

            engine.disableVibration()
            advanceTimeBy(1)
        }
    }

    /**
     * Property: Re-enabling after disable resumes pattern execution.
     */
    @Test
    fun testReEnablingResumesPatternExecution() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)

        engine.setPattern(VibrationPattern.Constant)
        engine.setIntensity(0.7f)

        // First enable/disable cycle
        engine.enableVibration()
        advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 3)
        engine.disableVibration()
        advanceTimeBy(1)

        controller.reset()

        // Second enable — must resume execution
        engine.enableVibration()
        advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 3)

        val patternCommands = controller.commands.filter { it.leftMotor != 0f || it.rightMotor != 0f }
        assertTrue(
            patternCommands.isNotEmpty(),
            "Pattern must execute again after re-enabling"
        )

        engine.disableVibration()
        advanceTimeBy(1)
    }
}


