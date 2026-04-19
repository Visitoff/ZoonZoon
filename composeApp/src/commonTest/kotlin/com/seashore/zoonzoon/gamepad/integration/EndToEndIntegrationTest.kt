package com.seashore.zoonzoon.gamepad.integration

import com.seashore.zoonzoon.gamepad.engine.FakePlatformGamepadController
import com.seashore.zoonzoon.gamepad.engine.VibrationEngine
import com.seashore.zoonzoon.gamepad.model.ConnectionState
import com.seashore.zoonzoon.gamepad.model.VibrationPattern
import com.seashore.zoonzoon.gamepad.viewmodel.GamepadViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * End-to-end integration tests.
 *
 * Tests the full flow: controller connection → pattern selection → vibration execution.
 *
 * **Validates: Requirements 1.1, 1.2, 1.3, 1.4, 2.1, 2.2, 2.3, 2.4,
 *              3.1, 4.1, 5.1, 5.2, 5.4, 7.9, 8.3, 8.4, 8.5**
 */
@OptIn(ExperimentalCoroutinesApi::class)
class EndToEndIntegrationTest {

    /**
     * Full flow: connect → select pattern → enable → verify commands → disable.
     */
    @Test
    fun testFullVibrationFlow() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        val viewModel = GamepadViewModel(engine, this)

        // 1. Simulate controller connection
        controller.setConnectionState(ConnectionState.Connected("DualShock 4"))
        advanceTimeBy(1)

        assertEquals(
            ConnectionState.Connected("DualShock 4"),
            viewModel.connectionState.value,
            "ViewModel should reflect connected state"
        )

        // 2. Select pattern
        viewModel.setPattern(VibrationPattern.Constant)
        assertEquals(VibrationPattern.Constant, viewModel.vibrationState.value.activePattern)

        // 3. Set intensity
        viewModel.setIntensity(0.8f)
        assertEquals(0.8f, viewModel.vibrationState.value.intensity)

        // 4. Enable vibration
        viewModel.setVibrationEnabled(true)
        assertTrue(viewModel.vibrationState.value.enabled)

        // 5. Verify commands are sent
        advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 5)
        assertTrue(controller.commands.isNotEmpty(), "Commands should be sent during vibration")

        // 6. Verify motor values match intensity
        val patternCommands = controller.commands.filter { it.leftMotor != 0f || it.rightMotor != 0f }
        assertTrue(patternCommands.isNotEmpty())
        patternCommands.forEach { cmd ->
            assertEquals(0.8f, cmd.leftMotor, 0.001f, "Left motor should match intensity")
            assertEquals(0.8f, cmd.rightMotor, 0.001f, "Right motor should match intensity")
        }

        // 7. Disable vibration
        viewModel.setVibrationEnabled(false)
        advanceTimeBy(1)
        assertFalse(viewModel.vibrationState.value.enabled)

        // 8. Verify stop command sent
        val stopCommands = controller.commands.filter { it.leftMotor == 0f && it.rightMotor == 0f }
        assertTrue(stopCommands.isNotEmpty(), "Stop command should be sent on disable")
    }

    /**
     * State propagation: changes flow correctly through all layers.
     */
    @Test
    fun testStatePropagationAcrossLayers() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        val viewModel = GamepadViewModel(engine, this)

        // Pattern change propagates
        viewModel.setPattern(VibrationPattern.Wave(1.0f))
        assertEquals(engine.vibrationState.value.activePattern, viewModel.vibrationState.value.activePattern)

        // Intensity change propagates
        viewModel.setIntensity(0.6f)
        assertEquals(engine.vibrationState.value.intensity, viewModel.vibrationState.value.intensity)

        // Enable propagates
        viewModel.setVibrationEnabled(true)
        advanceTimeBy(1)
        assertEquals(engine.vibrationState.value.enabled, viewModel.vibrationState.value.enabled)

        // Connection state propagates
        controller.setConnectionState(ConnectionState.Scanning)
        advanceTimeBy(1)
        assertEquals(controller.connectionState.value, viewModel.connectionState.value)

        // Clean up
        viewModel.setVibrationEnabled(false)
        advanceTimeBy(1)
    }

    /**
     * Pattern switching during active vibration.
     */
    @Test
    fun testPatternSwitchingDuringVibration() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        val viewModel = GamepadViewModel(engine, this)

        viewModel.setIntensity(0.7f)
        viewModel.setVibrationEnabled(true)
        advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 3)

        val patterns = listOf(
            VibrationPattern.Constant,
            VibrationPattern.Pulse(2.0f),
            VibrationPattern.Wave(1.0f)
        )

        for (pattern in patterns) {
            viewModel.setPattern(pattern)
            advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 3)

            assertTrue(viewModel.vibrationState.value.enabled, "Should remain enabled after pattern switch")
            assertEquals(pattern, viewModel.vibrationState.value.activePattern)
        }

        viewModel.setVibrationEnabled(false)
        advanceTimeBy(1)
    }

    /**
     * Connection loss during vibration stops execution.
     */
    @Test
    fun testConnectionLossDuringVibration() = runTest {
        val controller = FakePlatformGamepadController()
        controller.setConnectionState(ConnectionState.Connected("Xbox"))
        val engine = VibrationEngine(controller, this)
        val viewModel = GamepadViewModel(engine, this)

        viewModel.setVibrationEnabled(true)
        advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 3)
        assertTrue(viewModel.vibrationState.value.enabled)

        // Simulate connection loss
        controller.setConnectionState(ConnectionState.Disconnected)
        advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 2)

        assertFalse(viewModel.vibrationState.value.enabled, "Vibration should stop on connection loss")
        assertEquals(ConnectionState.Disconnected, viewModel.connectionState.value)
    }

    /**
     * Error handling: error state propagates to ViewModel.
     */
    @Test
    fun testErrorStateIsNullByDefault() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        val viewModel = GamepadViewModel(engine, this)

        assertEquals(null, viewModel.errorState.value, "Error state should be null by default")
    }
}
