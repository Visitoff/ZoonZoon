package com.seashore.zoonzoon.gamepad.engine

import com.seashore.zoonzoon.gamepad.model.VibrationPattern
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Property-based tests for continuous pattern execution.
 *
 * **Validates: Requirement 5.4**
 *
 * Property 12: Continuous Pattern Execution
 * For any vibration pattern, the VibrationEngine SHALL execute pattern frames
 * continuously until vibration is explicitly disabled.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ContinuousPatternExecutionPropertyTest {

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
     * Property: Pattern keeps sending commands across multiple frame intervals.
     */
    @Test
    fun testPatternSendsCommandsAcrossMultipleFrames() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)

        val patterns = listOf(
            VibrationPattern.Constant,
            VibrationPattern.Pulse(2.0f),
            VibrationPattern.Wave(1.0f),
            VibrationPattern.Custom(
                listOf(VibrationPattern.Custom.Frame(0.5f, 0.5f, 100L)),
                loop = true
            )
        )

        for (pattern in patterns) {
            controller.reset()
            engine.setPattern(pattern)
            engine.setIntensity(0.7f)
            engine.enableVibration()

            // Advance 5 frames
            advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 5)

            // Should have received multiple commands (at least 3)
            val patternCommands = controller.commands.filter { it.leftMotor != 0f || it.rightMotor != 0f }
            assertTrue(
                patternCommands.size >= 3,
                "Pattern ${pattern::class.simpleName} should send at least 3 commands over 5 frames, got ${patternCommands.size}"
            )

            engine.disableVibration()
            advanceTimeBy(1)
        }
    }

    /**
     * Property: Pattern stops sending commands after disableVibration() is called.
     */
    @Test
    fun testPatternStopsAfterDisable() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)

        engine.setPattern(VibrationPattern.Constant)
        engine.setIntensity(0.8f)
        engine.enableVibration()
        advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 5)

        engine.disableVibration()
        advanceTimeBy(1)

        val countAtDisable = controller.commands.size

        // Advance more time — no new pattern commands should arrive
        advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 10)

        val newCommands = controller.commands.drop(countAtDisable)
            .filter { it.leftMotor != 0f || it.rightMotor != 0f }

        assertTrue(
            newCommands.isEmpty(),
            "No pattern commands should be sent after disableVibration(), got: $newCommands"
        )
    }

    /**
     * Property: Pattern continues executing over a longer duration without stopping.
     */
    @Test
    fun testPatternContinuesOverLongerDuration() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)

        engine.setPattern(VibrationPattern.Constant)
        engine.setIntensity(0.5f)
        engine.enableVibration()

        // Sample command counts at different time points
        advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 5)
        val count5 = controller.commands.filter { it.leftMotor != 0f || it.rightMotor != 0f }.size

        advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 5)
        val count10 = controller.commands.filter { it.leftMotor != 0f || it.rightMotor != 0f }.size

        advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 10)
        val count20 = controller.commands.filter { it.leftMotor != 0f || it.rightMotor != 0f }.size

        // Each window should have more commands than the previous
        assertTrue(count10 > count5, "More commands at 10 frames than at 5 frames")
        assertTrue(count20 > count10, "More commands at 20 frames than at 10 frames")

        engine.disableVibration()
        advanceTimeBy(1)
    }
}


