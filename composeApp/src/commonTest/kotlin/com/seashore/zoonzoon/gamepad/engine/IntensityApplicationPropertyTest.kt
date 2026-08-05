package com.seashore.zoonzoon.gamepad.engine

import com.seashore.zoonzoon.gamepad.model.VibrationPattern
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Property-based tests for runtime intensity application.
 *
 * **Validates: Requirement 4.3**
 *
 * Property 9: Runtime Intensity Application
 * For any intensity change during active vibration, the VibrationEngine SHALL
 * apply the new intensity to all subsequent vibration commands.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class IntensityApplicationPropertyTest {

    private class FakeGamepadController : GamepadController {
        data class Command(val leftMotor: Float, val rightMotor: Float)
        val commands = mutableListOf<Command>()
        override suspend fun sendVibrationCommand(leftMotor: Float, rightMotor: Float, sharpness: Float): Result<Unit> {
            commands.add(Command(leftMotor, rightMotor))
            return Result.success(Unit)
        }
        fun reset() = commands.clear()
    }

    /**
     * Property: Changing intensity during active vibration applies to subsequent commands.
     * Uses Constant pattern so motor values == intensity, making verification straightforward.
     */
    @Test
    fun testIntensityChangeAppliedToSubsequentCommands() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)

        engine.setPattern(VibrationPattern.Constant)
        engine.setIntensity(1.0f)
        engine.enableVibration()

        // Let a few frames run at full intensity
        advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 3)
        val countBefore = controller.commands.size

        // Change intensity mid-execution
        engine.setIntensity(0.3f)
        advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 3)

        val commandsAfter = controller.commands.drop(countBefore)
            .filter { it.leftMotor != 0f || it.rightMotor != 0f }

        assertTrue(commandsAfter.isNotEmpty(), "Should have commands after intensity change")
        commandsAfter.forEach { cmd ->
            assertEquals(
                0.3f, cmd.leftMotor, 0.001f,
                "Left motor must reflect new intensity 0.3, got ${cmd.leftMotor}"
            )
            assertEquals(
                0.3f, cmd.rightMotor, 0.001f,
                "Right motor must reflect new intensity 0.3, got ${cmd.rightMotor}"
            )
        }

        engine.disableVibration()
        advanceTimeBy(1)
    }

    /**
     * Property: Multiple intensity changes are each applied to subsequent commands.
     */
    @Test
    fun testMultipleIntensityChangesAppliedInOrder() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)

        engine.setPattern(VibrationPattern.Constant)
        engine.setIntensity(1.0f)
        engine.enableVibration()

        val intensitySteps = listOf(0.8f, 0.5f, 0.2f)

        for (intensity in intensitySteps) {
            advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 2)
            val countBefore = controller.commands.size

            engine.setIntensity(intensity)
            advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 2)

            val commandsAfter = controller.commands.drop(countBefore)
                .filter { it.leftMotor != 0f || it.rightMotor != 0f }

            assertTrue(commandsAfter.isNotEmpty(), "Should have commands after setting intensity=$intensity")
            commandsAfter.forEach { cmd ->
                assertEquals(
                    intensity, cmd.leftMotor, 0.001f,
                    "Motor value must equal intensity=$intensity after change"
                )
            }
        }

        engine.disableVibration()
        advanceTimeBy(1)
    }

    /**
     * Property: Setting intensity to 0 during active vibration produces zero motor values.
     */
    @Test
    fun testZeroIntensityDuringActiveVibrationProducesZeroMotors() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)

        engine.setPattern(VibrationPattern.Constant)
        engine.setIntensity(1.0f)
        engine.enableVibration()
        advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 2)

        val countBefore = controller.commands.size
        engine.setIntensity(0.0f)
        advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 3)

        val commandsAfter = controller.commands.drop(countBefore)
        // All commands after setting intensity=0 must have zero motor values
        commandsAfter.forEach { cmd ->
            assertEquals(0.0f, cmd.leftMotor, 0.001f, "Left motor must be 0 at zero intensity")
            assertEquals(0.0f, cmd.rightMotor, 0.001f, "Right motor must be 0 at zero intensity")
        }

        engine.disableVibration()
        advanceTimeBy(1)
    }

    /**
     * Property: Intensity change applies immediately on the next frame, not with delay.
     */
    @Test
    fun testIntensityChangeAppliesOnNextFrame() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)

        engine.setPattern(VibrationPattern.Constant)
        engine.setIntensity(1.0f)
        engine.enableVibration()
        advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 2)

        val countBefore = controller.commands.size
        engine.setIntensity(0.4f)

        // Advance exactly one frame
        advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS + 1)

        val commandsAfter = controller.commands.drop(countBefore)
            .filter { it.leftMotor != 0f || it.rightMotor != 0f }

        assertTrue(commandsAfter.isNotEmpty(), "Should have at least one command after one frame")
        assertEquals(
            0.4f, commandsAfter.first().leftMotor, 0.001f,
            "First command after intensity change must use new intensity"
        )

        engine.disableVibration()
        advanceTimeBy(1)
    }
}


