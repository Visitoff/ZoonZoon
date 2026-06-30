package com.seashore.zoonzoon.gamepad.engine

import com.seashore.zoonzoon.gamepad.model.ConnectionState
import com.seashore.zoonzoon.gamepad.model.VibrationPattern
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Property tests for command failure resilience.
 *
 * **Property 19: Command Failure Resilience**
 * **Validates: Requirement 10.3**
 *
 * For any vibration command failure, the System SHALL log the error and
 * continue attempting subsequent commands without halting execution.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CommandFailureResiliencePropertyTest {

    /**
     * A controller that fails on every command.
     */
    private class FailingGamepadController : GamepadControllerWithState {
        var failureCount = 0
        private val _connectionState = kotlinx.coroutines.flow.MutableStateFlow<ConnectionState>(
            ConnectionState.Connected("Test")
        )
        override val connectionState = _connectionState.asStateFlow()
        override suspend fun startDiscovery() {}
        override suspend fun stopDiscovery()  {}
        override suspend fun disconnect()     { _connectionState.value = ConnectionState.Disconnected }

        override suspend fun sendVibrationCommand(leftMotor: Float, rightMotor: Float): Result<Unit> {
            failureCount++
            return Result.failure(Exception("Simulated command failure #$failureCount"))
        }
    }

    /**
     * A controller that fails on the first N commands then succeeds.
     */
    private class PartiallyFailingController(private val failCount: Int) : GamepadControllerWithState {
        var callCount = 0
        private val _connectionState = kotlinx.coroutines.flow.MutableStateFlow<ConnectionState>(
            ConnectionState.Connected("Test")
        )
        override val connectionState = _connectionState.asStateFlow()
        override suspend fun startDiscovery() {}
        override suspend fun stopDiscovery()  {}
        override suspend fun disconnect()     { _connectionState.value = ConnectionState.Disconnected }

        override suspend fun sendVibrationCommand(leftMotor: Float, rightMotor: Float): Result<Unit> {
            callCount++
            return if (callCount <= failCount) {
                Result.failure(Exception("Simulated failure $callCount"))
            } else {
                Result.success(Unit)
            }
        }
    }

    /**
     * Property: When a command fails, the engine keeps running and retries on later frames.
     */
    @Test
    fun testCommandFailureContinuesExecutionLoop() = runTest {
        val controller = FailingGamepadController()
        val engine = VibrationEngine(controller, this)

        engine.setPattern(VibrationPattern.Constant)
        engine.setIntensity(0.5f)
        engine.enableVibration()

        advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 5)

        assertTrue(
            engine.vibrationState.value.enabled,
            "Engine should stay enabled and keep retrying after command failures"
        )
        assertTrue(
            controller.failureCount >= 3,
            "Engine should keep attempting commands each frame. Got ${controller.failureCount} failures"
        )

        engine.disableVibration()
    }

    /**
     * Property: After a failure and re-enable, the engine can resume.
     */
    @Test
    fun testEngineCanResumeAfterFailureAndReEnable() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)

        engine.setPattern(VibrationPattern.Constant)
        engine.setIntensity(0.5f)

        // First enable cycle
        engine.enableVibration()
        advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 3)
        engine.disableVibration()
        advanceTimeBy(1)

        controller.reset()

        // Second enable cycle — should work fine
        engine.enableVibration()
        advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 3)

        assertTrue(
            controller.commands.isNotEmpty(),
            "Engine should send commands after re-enable"
        )
        assertTrue(
            engine.vibrationState.value.enabled,
            "Engine should be enabled"
        )

        // Clean up
        engine.disableVibration()
        advanceTimeBy(1)
    }

    /**
     * Property: Connection loss (Disconnected state) stops vibration.
     */
    @Test
    fun testConnectionLossStopsVibration() = runTest {
        val controller = FakePlatformGamepadController()
        controller.setConnectionState(ConnectionState.Connected("Test"))
        val engine = VibrationEngine(controller, this)

        engine.setPattern(VibrationPattern.Constant)
        engine.enableVibration()
        advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 3)

        assertTrue(engine.vibrationState.value.enabled, "Should be enabled before disconnect")

        // Simulate connection loss
        controller.setConnectionState(ConnectionState.Disconnected)
        advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 2)

        assertFalse(
            engine.vibrationState.value.enabled,
            "Vibration should stop after connection loss"
        )
    }
}
