package com.seashore.zoonzoon.gamepad.viewmodel

import com.seashore.zoonzoon.gamepad.engine.FakePlatformGamepadController
import com.seashore.zoonzoon.gamepad.engine.VibrationEngine
import com.seashore.zoonzoon.gamepad.model.ConnectionState
import com.seashore.zoonzoon.gamepad.model.VibrationPattern
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for GamepadViewModel.
 *
 * **Validates: Requirements 8.1, 8.2, 8.3, 8.4, 8.5**
 *
 * These tests verify:
 * - State initialization
 * - All user action methods (setVibrationEnabled, setPattern, setIntensity)
 * - State update propagation from VibrationEngine to ViewModel
 */
@OptIn(ExperimentalCoroutinesApi::class)
class GamepadViewModelTest {

    // -------------------------------------------------------------------------
    // State Initialization Tests
    // -------------------------------------------------------------------------

    /**
     * Test that ViewModel initializes with default VibrationState.
     */
    @Test
    fun testInitialVibrationState() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        val viewModel = GamepadViewModel(engine, this)

        advanceTimeBy(1) // Allow initialization

        val state = viewModel.vibrationState.value
        assertFalse(state.enabled, "Vibration should be disabled initially")
        assertEquals(
            VibrationPattern.Constant,
            state.activePattern,
            "Default pattern should be Constant"
        )
        assertEquals(0.7f, state.intensity, "Default intensity should be 0.7")
    }

    /**
     * Test that ViewModel initializes with controller's ConnectionState.
     */
    @Test
    fun testInitialConnectionState() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        val viewModel = GamepadViewModel(engine, this)

        advanceTimeBy(1) // Allow initialization

        // FakePlatformGamepadController defaults to Connected state
        assertTrue(
            viewModel.connectionState.value is ConnectionState.Connected,
            "Initial connection state should be Connected"
        )
    }

    /**
     * Test that ViewModel can be initialized with a disconnected controller.
     */
    @Test
    fun testInitializationWithDisconnectedController() = runTest {
        val controller = FakePlatformGamepadController()
        controller.setConnectionState(ConnectionState.Disconnected)
        val engine = VibrationEngine(controller, this)
        val viewModel = GamepadViewModel(engine, this)

        advanceTimeBy(1)

        assertEquals(
            ConnectionState.Disconnected,
            viewModel.connectionState.value,
            "Connection state should be Disconnected"
        )
    }

    // -------------------------------------------------------------------------
    // setVibrationEnabled Tests
    // -------------------------------------------------------------------------

    /**
     * Test enabling vibration updates the state.
     */
    @Test
    fun testEnableVibration() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        val viewModel = GamepadViewModel(engine, this)

        viewModel.setVibrationEnabled(true)
        advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 2)

        assertTrue(
            viewModel.vibrationState.value.enabled,
            "Vibration should be enabled"
        )
        
        // Cleanup: disable vibration to stop the execution loop
        viewModel.setVibrationEnabled(false)
        advanceTimeBy(1)
    }

    /**
     * Test disabling vibration updates the state.
     */
    @Test
    fun testDisableVibration() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        val viewModel = GamepadViewModel(engine, this)

        // Enable first
        viewModel.setVibrationEnabled(true)
        advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 2)
        assertTrue(viewModel.vibrationState.value.enabled)

        // Then disable
        viewModel.setVibrationEnabled(false)
        advanceTimeBy(1)

        assertFalse(
            viewModel.vibrationState.value.enabled,
            "Vibration should be disabled"
        )
    }

    /**
     * Test enabling vibration sends commands to controller.
     */
    @Test
    fun testEnableVibrationSendsCommands() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        val viewModel = GamepadViewModel(engine, this)

        viewModel.setVibrationEnabled(true)
        advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 3)

        assertTrue(
            controller.commands.isNotEmpty(),
            "Commands should be sent when vibration is enabled"
        )

        // Clean up
        viewModel.setVibrationEnabled(false)
        advanceTimeBy(1)
    }

    /**
     * Test disabling vibration sends stop command.
     */
    @Test
    fun testDisableVibrationSendsStopCommand() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        val viewModel = GamepadViewModel(engine, this)

        // Enable and let it run
        viewModel.setVibrationEnabled(true)
        advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 3)

        // Disable
        viewModel.setVibrationEnabled(false)
        advanceTimeBy(1)

        val lastCommand = controller.getLastCommand()
        assertEquals(
            0f,
            lastCommand?.leftMotor,
            "Last command should have left motor = 0"
        )
        assertEquals(
            0f,
            lastCommand?.rightMotor,
            "Last command should have right motor = 0"
        )
    }

    /**
     * Test toggling vibration multiple times.
     */
    @Test
    fun testToggleVibrationMultipleTimes() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        val viewModel = GamepadViewModel(engine, this)

        for (i in 1..3) {
            // Enable
            viewModel.setVibrationEnabled(true)
            advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 2)
            assertTrue(
                viewModel.vibrationState.value.enabled,
                "Vibration should be enabled on iteration $i"
            )

            // Disable
            viewModel.setVibrationEnabled(false)
            advanceTimeBy(1)
            assertFalse(
                viewModel.vibrationState.value.enabled,
                "Vibration should be disabled on iteration $i"
            )
        }
    }

    // -------------------------------------------------------------------------
    // setPattern Tests
    // -------------------------------------------------------------------------

    /**
     * Test setting pattern to Constant.
     */
    @Test
    fun testSetPatternConstant() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        val viewModel = GamepadViewModel(engine, this)

        viewModel.setPattern(VibrationPattern.Constant)
        advanceTimeBy(1)

        assertEquals(
            VibrationPattern.Constant,
            viewModel.vibrationState.value.activePattern,
            "Active pattern should be Constant"
        )
    }

    /**
     * Test setting pattern to Pulse.
     */
    @Test
    fun testSetPatternPulse() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        val viewModel = GamepadViewModel(engine, this)

        val pulsePattern = VibrationPattern.Pulse(frequencyHz = 2.0f)
        viewModel.setPattern(pulsePattern)
        advanceTimeBy(1)

        assertEquals(
            pulsePattern,
            viewModel.vibrationState.value.activePattern,
            "Active pattern should be Pulse"
        )
    }

    /**
     * Test setting pattern to Wave.
     */
    @Test
    fun testSetPatternWave() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        val viewModel = GamepadViewModel(engine, this)

        val wavePattern = VibrationPattern.Wave(frequencyHz = 1.0f)
        viewModel.setPattern(wavePattern)
        advanceTimeBy(1)

        assertEquals(
            wavePattern,
            viewModel.vibrationState.value.activePattern,
            "Active pattern should be Wave"
        )
    }

    /**
     * Test setting pattern to Custom.
     */
    @Test
    fun testSetPatternCustom() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        val viewModel = GamepadViewModel(engine, this)

        val customPattern = VibrationPattern.Custom(
            listOf(
                VibrationPattern.Custom.Frame(0.5f, 0.5f, 100L),
                VibrationPattern.Custom.Frame(0.8f, 0.2f, 100L)
            ),
            loop = true
        )
        viewModel.setPattern(customPattern)
        advanceTimeBy(1)

        assertEquals(
            customPattern,
            viewModel.vibrationState.value.activePattern,
            "Active pattern should be Custom"
        )
    }

    /**
     * Test changing patterns multiple times.
     */
    @Test
    fun testChangePatternsMultipleTimes() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        val viewModel = GamepadViewModel(engine, this)

        val patterns = listOf(
            VibrationPattern.Constant,
            VibrationPattern.Pulse(frequencyHz = 2.0f),
            VibrationPattern.Wave(frequencyHz = 1.0f),
            VibrationPattern.Constant
        )

        for (pattern in patterns) {
            viewModel.setPattern(pattern)
            advanceTimeBy(1)
            assertEquals(
                pattern,
                viewModel.vibrationState.value.activePattern,
                "Pattern should be ${pattern::class.simpleName}"
            )
        }
    }

    /**
     * Test setting pattern while vibration is active.
     */
    @Test
    fun testSetPatternWhileVibrating() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        val viewModel = GamepadViewModel(engine, this)

        viewModel.setVibrationEnabled(true)
        advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 2)

        val newPattern = VibrationPattern.Pulse(frequencyHz = 3.0f)
        viewModel.setPattern(newPattern)
        advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 2)

        assertEquals(newPattern, viewModel.vibrationState.value.activePattern, "Pattern should change while vibrating")
        assertTrue(viewModel.vibrationState.value.enabled, "Vibration should remain enabled")

        // Clean up
        viewModel.setVibrationEnabled(false)
        advanceTimeBy(1)
    }

    // -------------------------------------------------------------------------
    // setIntensity Tests
    // -------------------------------------------------------------------------

    /**
     * Test setting valid intensity values.
     */
    @Test
    fun testSetValidIntensity() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        val viewModel = GamepadViewModel(engine, this)

        val intensities = listOf(0.0f, 0.25f, 0.5f, 0.75f, 1.0f)

        for (intensity in intensities) {
            viewModel.setIntensity(intensity)
            advanceTimeBy(1)
            assertEquals(
                intensity,
                viewModel.vibrationState.value.intensity,
                "Intensity should be $intensity"
            )
        }
    }

    /**
     * Test setting intensity below 0.0 throws exception.
     */
    @Test
    fun testSetIntensityBelowZeroThrows() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        val viewModel = GamepadViewModel(engine, this)

        assertFailsWith<IllegalArgumentException> {
            viewModel.setIntensity(-0.1f)
        }
    }

    /**
     * Test setting intensity above 1.0 throws exception.
     */
    @Test
    fun testSetIntensityAboveOneThrows() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        val viewModel = GamepadViewModel(engine, this)

        assertFailsWith<IllegalArgumentException> {
            viewModel.setIntensity(1.1f)
        }
    }

    /**
     * Test changing intensity multiple times.
     */
    @Test
    fun testChangeIntensityMultipleTimes() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        val viewModel = GamepadViewModel(engine, this)

        val intensities = listOf(0.2f, 0.5f, 0.8f, 0.3f, 0.9f)

        for (intensity in intensities) {
            viewModel.setIntensity(intensity)
            advanceTimeBy(1)
            assertEquals(
                intensity,
                viewModel.vibrationState.value.intensity,
                "Intensity should be $intensity"
            )
        }
    }

    /**
     * Test setting intensity while vibration is active.
     */
    @Test
    fun testSetIntensityWhileVibrating() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        val viewModel = GamepadViewModel(engine, this)

        viewModel.setVibrationEnabled(true)
        advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 2)

        viewModel.setIntensity(0.9f)
        advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 2)

        assertEquals(0.9f, viewModel.vibrationState.value.intensity, "Intensity should change while vibrating")
        assertTrue(viewModel.vibrationState.value.enabled, "Vibration should remain enabled")

        // Clean up
        viewModel.setVibrationEnabled(false)
        advanceTimeBy(1)
    }

    // -------------------------------------------------------------------------
    // State Update Propagation Tests
    // -------------------------------------------------------------------------

    /**
     * Test that connection state updates propagate from controller to ViewModel.
     */
    @Test
    fun testConnectionStateUpdatePropagation() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        val viewModel = GamepadViewModel(engine, this)

        // Change connection state
        controller.setConnectionState(ConnectionState.Disconnected)
        advanceTimeBy(1)

        assertEquals(
            ConnectionState.Disconnected,
            viewModel.connectionState.value,
            "Connection state should propagate to ViewModel"
        )

        // Change to Scanning
        controller.setConnectionState(ConnectionState.Scanning)
        advanceTimeBy(1)

        assertEquals(
            ConnectionState.Scanning,
            viewModel.connectionState.value,
            "Connection state should update to Scanning"
        )

        // Change to Connected
        val connectedState = ConnectionState.Connected("DualShock 4")
        controller.setConnectionState(connectedState)
        advanceTimeBy(1)

        assertEquals(
            connectedState,
            viewModel.connectionState.value,
            "Connection state should update to Connected"
        )
    }

    /**
     * Test that vibration state updates propagate from engine to ViewModel.
     */
    @Test
    fun testVibrationStateUpdatePropagation() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        val viewModel = GamepadViewModel(engine, this)

        viewModel.setVibrationEnabled(true)
        advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 2)

        assertTrue(viewModel.vibrationState.value.enabled, "Enabled state should propagate")

        val newPattern = VibrationPattern.Pulse(frequencyHz = 2.0f)
        viewModel.setPattern(newPattern)
        advanceTimeBy(1)

        assertEquals(newPattern, viewModel.vibrationState.value.activePattern, "Pattern state should propagate")

        viewModel.setIntensity(0.8f)
        advanceTimeBy(1)

        assertEquals(0.8f, viewModel.vibrationState.value.intensity, "Intensity state should propagate")

        // Clean up
        viewModel.setVibrationEnabled(false)
        advanceTimeBy(1)
    }

    /**
     * Test that connection loss during vibration updates state correctly.
     */
    @Test
    fun testConnectionLossDuringVibrationUpdatesState() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        val viewModel = GamepadViewModel(engine, this)

        // Enable vibration
        viewModel.setVibrationEnabled(true)
        advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 2)
        assertTrue(viewModel.vibrationState.value.enabled)

        // Simulate connection loss
        controller.setConnectionState(ConnectionState.Disconnected)
        advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 2)

        // Verify connection state updated
        assertEquals(
            ConnectionState.Disconnected,
            viewModel.connectionState.value,
            "Connection state should be Disconnected"
        )

        // Verify vibration stopped (engine behavior)
        assertFalse(
            viewModel.vibrationState.value.enabled,
            "Vibration should be disabled after connection loss"
        )
    }

    /**
     * Test that state updates are reflected immediately.
     */
    @Test
    fun testStateUpdatesAreImmediate() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        val viewModel = GamepadViewModel(engine, this)

        // Test immediate pattern update
        val pattern = VibrationPattern.Pulse(frequencyHz = 2.0f)
        viewModel.setPattern(pattern)
        advanceTimeBy(1)
        assertEquals(pattern, viewModel.vibrationState.value.activePattern)

        // Test immediate intensity update
        viewModel.setIntensity(0.6f)
        advanceTimeBy(1)
        assertEquals(0.6f, viewModel.vibrationState.value.intensity)

        // Test immediate connection state update
        controller.setConnectionState(ConnectionState.Scanning)
        advanceTimeBy(1)
        assertEquals(ConnectionState.Scanning, viewModel.connectionState.value)
    }
}
