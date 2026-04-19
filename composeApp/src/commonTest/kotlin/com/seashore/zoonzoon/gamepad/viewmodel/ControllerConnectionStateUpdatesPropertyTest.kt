package com.seashore.zoonzoon.gamepad.viewmodel

import com.seashore.zoonzoon.gamepad.engine.FakePlatformGamepadController
import com.seashore.zoonzoon.gamepad.engine.VibrationEngine
import com.seashore.zoonzoon.gamepad.model.ConnectionState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Property-based tests for controller connection state updates.
 *
 * **Validates: Requirements 1.2, 1.3, 1.4**
 *
 * Property 1: Controller Connection State Updates
 * For any controller connection or disconnection event, the System SHALL update
 * the ConnectionState and the UI_Layer SHALL display the corresponding status
 * (connected or disconnected).
 *
 * This test validates that the ViewModel correctly exposes connection state changes
 * from the underlying controller, ensuring the UI can reactively display connection status.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ControllerConnectionStateUpdatesPropertyTest {

    /**
     * Property: ViewModel exposes connection state for all possible states
     *
     * Tests that the ViewModel correctly exposes all connection states
     * (Disconnected, Scanning, Connected) from the controller.
     */
    @Test
    fun testViewModelExposesAllConnectionStates() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        val viewModel = GamepadViewModel(engine, this)

        // Test all connection states
        val states = listOf(
            ConnectionState.Disconnected,
            ConnectionState.Scanning,
            ConnectionState.Connected("DualShock 4"),
            ConnectionState.Connected("DualSense"),
            ConnectionState.Connected("Xbox Controller"),
            ConnectionState.Disconnected
        )

        for (state in states) {
            controller.setConnectionState(state)
            advanceTimeBy(1) // Allow state to propagate

            assertEquals(
                state,
                viewModel.connectionState.value,
                "ViewModel should expose connection state: $state"
            )
        }
    }

    /**
     * Property: Connection state updates propagate immediately
     *
     * Tests that connection state changes in the controller are immediately
     * reflected in the ViewModel's exposed state.
     */
    @Test
    fun testConnectionStateUpdatesPropagate() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        val viewModel = GamepadViewModel(engine, this)

        // Initial state should be Connected (FakePlatformGamepadController default)
        assertTrue(
            viewModel.connectionState.value is ConnectionState.Connected,
            "Initial state should be Connected"
        )

        // Change to Disconnected
        controller.setConnectionState(ConnectionState.Disconnected)
        advanceTimeBy(1)

        assertEquals(
            ConnectionState.Disconnected,
            viewModel.connectionState.value,
            "State should update to Disconnected"
        )

        // Change to Scanning
        controller.setConnectionState(ConnectionState.Scanning)
        advanceTimeBy(1)

        assertEquals(
            ConnectionState.Scanning,
            viewModel.connectionState.value,
            "State should update to Scanning"
        )

        // Change to Connected with specific controller type
        val connectedState = ConnectionState.Connected("Test Controller")
        controller.setConnectionState(connectedState)
        advanceTimeBy(1)

        assertEquals(
            connectedState,
            viewModel.connectionState.value,
            "State should update to Connected with controller type"
        )
    }

    /**
     * Property: Multiple connection/disconnection cycles maintain state consistency
     *
     * Tests that the ViewModel correctly tracks connection state through multiple
     * connection and disconnection cycles.
     */
    @Test
    fun testMultipleConnectionCyclesMaintainConsistency() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        val viewModel = GamepadViewModel(engine, this)

        val controllerTypes = listOf("DualShock 4", "DualSense", "Xbox Controller")

        for (controllerType in controllerTypes) {
            // Disconnect
            controller.setConnectionState(ConnectionState.Disconnected)
            advanceTimeBy(1)
            assertEquals(
                ConnectionState.Disconnected,
                viewModel.connectionState.value,
                "Should be disconnected before connecting $controllerType"
            )

            // Scan
            controller.setConnectionState(ConnectionState.Scanning)
            advanceTimeBy(1)
            assertEquals(
                ConnectionState.Scanning,
                viewModel.connectionState.value,
                "Should be scanning for $controllerType"
            )

            // Connect
            val connectedState = ConnectionState.Connected(controllerType)
            controller.setConnectionState(connectedState)
            advanceTimeBy(1)
            assertEquals(
                connectedState,
                viewModel.connectionState.value,
                "Should be connected to $controllerType"
            )
        }
    }

    /**
     * Property: Connection state updates during active vibration
     *
     * Tests that connection state changes are correctly exposed even when
     * vibration is actively running.
     */
    @Test
    fun testConnectionStateUpdatesDuringActiveVibration() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        val viewModel = GamepadViewModel(engine, this)

        // Start vibration
        viewModel.setVibrationEnabled(true)
        advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 2)

        // Verify vibration is active
        assertTrue(
            viewModel.vibrationState.value.enabled,
            "Vibration should be enabled"
        )

        // Change connection state while vibrating
        controller.setConnectionState(ConnectionState.Disconnected)
        advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 2)

        // Verify connection state updated
        assertEquals(
            ConnectionState.Disconnected,
            viewModel.connectionState.value,
            "Connection state should update to Disconnected even during vibration"
        )

        // Verify vibration stopped (engine behavior)
        assertEquals(
            false,
            viewModel.vibrationState.value.enabled,
            "Vibration should be disabled after connection loss"
        )
    }

    /**
     * Property: Connection state reflects controller type information
     *
     * Tests that when a controller connects, the ViewModel exposes the
     * controller type information correctly.
     */
    @Test
    fun testConnectionStateReflectsControllerType() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        val viewModel = GamepadViewModel(engine, this)

        val controllerTypes = listOf(
            "DualShock 4",
            "DualSense",
            "Xbox Controller",
            "Generic HID Controller",
            "Custom Controller XYZ"
        )

        for (controllerType in controllerTypes) {
            val connectedState = ConnectionState.Connected(controllerType)
            controller.setConnectionState(connectedState)
            advanceTimeBy(1)

            val currentState = viewModel.connectionState.value
            assertTrue(
                currentState is ConnectionState.Connected,
                "State should be Connected for controller type: $controllerType"
            )
            assertEquals(
                controllerType,
                (currentState as ConnectionState.Connected).controllerType,
                "Controller type should match: $controllerType"
            )
        }
    }

    /**
     * Property: Rapid connection state changes are all reflected
     *
     * Tests that even rapid connection state changes are correctly propagated
     * to the ViewModel.
     */
    @Test
    fun testRapidConnectionStateChanges() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        val viewModel = GamepadViewModel(engine, this)

        val states = listOf(
            ConnectionState.Disconnected,
            ConnectionState.Scanning,
            ConnectionState.Connected("Controller 1"),
            ConnectionState.Disconnected,
            ConnectionState.Scanning,
            ConnectionState.Connected("Controller 2"),
            ConnectionState.Disconnected
        )

        for (state in states) {
            controller.setConnectionState(state)
            advanceTimeBy(1) // Minimal delay

            assertEquals(
                state,
                viewModel.connectionState.value,
                "ViewModel should reflect rapid state change to: $state"
            )
        }
    }

    /**
     * Property: Connection state is consistent across ViewModel and Engine
     *
     * Tests that the connection state exposed by the ViewModel is always
     * consistent with the state in the VibrationEngine.
     */
    @Test
    fun testConnectionStateConsistencyBetweenViewModelAndEngine() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        val viewModel = GamepadViewModel(engine, this)

        val states = listOf(
            ConnectionState.Disconnected,
            ConnectionState.Scanning,
            ConnectionState.Connected("Test Controller"),
            ConnectionState.Disconnected
        )

        for (state in states) {
            controller.setConnectionState(state)
            advanceTimeBy(1)

            assertEquals(
                engine.controller.connectionState.value,
                viewModel.connectionState.value,
                "ViewModel and Engine connection states should be consistent for: $state"
            )
        }
    }
}
