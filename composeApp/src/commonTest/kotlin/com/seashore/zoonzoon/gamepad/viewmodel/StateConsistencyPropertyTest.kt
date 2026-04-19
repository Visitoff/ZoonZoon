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

/**
 * Property-based tests for state consistency across layers.
 *
 * **Validates: Requirements 8.3, 8.4, 8.5**
 *
 * Property 17: State Consistency Across Layers
 * For any operation that modifies state, the System SHALL maintain consistency
 * between the ViewModel, VibrationEngine, and UI_Layer state representations.
 *
 * This test validates that state changes propagate correctly and consistently
 * across all layers of the system.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class StateConsistencyPropertyTest {

    /**
     * Property: VibrationState is consistent between ViewModel and Engine
     *
     * Tests that the VibrationState exposed by the ViewModel always matches
     * the VibrationState in the VibrationEngine.
     */
    @Test
    fun testVibrationStateConsistencyBetweenViewModelAndEngine() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        val viewModel = GamepadViewModel(engine, this)

        val operations: List<() -> Unit> = listOf(
            { viewModel.setVibrationEnabled(true) },
            { viewModel.setPattern(VibrationPattern.Pulse(frequencyHz = 2.0f)) },
            { viewModel.setIntensity(0.5f) },
            { viewModel.setVibrationEnabled(false) },
            { viewModel.setPattern(VibrationPattern.Wave(frequencyHz = 1.0f)) },
            { viewModel.setIntensity(0.8f) },
            { viewModel.setVibrationEnabled(true) },
            { viewModel.setIntensity(0.3f) },
            { viewModel.setPattern(VibrationPattern.Constant) }
        )

        for (operation in operations) {
            operation()
            advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 2)
            val vm = viewModel.vibrationState.value
            val eng = engine.vibrationState.value
            assertEquals(eng.enabled, vm.enabled, "Enabled state should be consistent")
            assertEquals(eng.activePattern, vm.activePattern, "Pattern should be consistent")
            assertEquals(eng.intensity, vm.intensity, "Intensity should be consistent")
        }

        // Clean up
        viewModel.setVibrationEnabled(false)
        advanceTimeBy(1)
    }

    /**
     * Property: ConnectionState is consistent between ViewModel and Engine
     *
     * Tests that the ConnectionState exposed by the ViewModel always matches
     * the ConnectionState from the controller.
     */
    @Test
    fun testConnectionStateConsistencyBetweenViewModelAndEngine() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        val viewModel = GamepadViewModel(engine, this)

        val states = listOf(
            ConnectionState.Disconnected,
            ConnectionState.Scanning,
            ConnectionState.Connected("DualShock 4"),
            ConnectionState.Disconnected,
            ConnectionState.Scanning,
            ConnectionState.Connected("DualSense"),
            ConnectionState.Connected("Xbox Controller"),
            ConnectionState.Disconnected
        )

        for (state in states) {
            controller.setConnectionState(state)
            advanceTimeBy(1)

            assertEquals(
                engine.controller.connectionState.value,
                viewModel.connectionState.value,
                "ConnectionState should be consistent for: $state"
            )
        }
    }

    /**
     * Property: State consistency is maintained during complex operation sequences
     *
     * Tests that state remains consistent across complex sequences of operations
     * involving multiple state changes.
     */
    @Test
    fun testStateConsistencyDuringComplexOperations() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        val viewModel = GamepadViewModel(engine, this)

        // Complex sequence: enable, change pattern, change intensity, disable, repeat
        val patterns = listOf(
            VibrationPattern.Constant,
            VibrationPattern.Pulse(frequencyHz = 2.0f),
            VibrationPattern.Wave(frequencyHz = 1.0f)
        )
        val intensities = listOf(0.3f, 0.6f, 0.9f)

        for ((index, pattern) in patterns.withIndex()) {
            val intensity = intensities[index]

            // Enable
            viewModel.setVibrationEnabled(true)
            advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 2)
            assertStateConsistency(viewModel, engine, "after enable")

            // Change pattern
            viewModel.setPattern(pattern)
            advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 2)
            assertStateConsistency(viewModel, engine, "after pattern change")

            // Change intensity
            viewModel.setIntensity(intensity)
            advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 2)
            assertStateConsistency(viewModel, engine, "after intensity change")

            // Disable
            viewModel.setVibrationEnabled(false)
            advanceTimeBy(1)
            assertStateConsistency(viewModel, engine, "after disable")
        }
    }

    /**
     * Property: State consistency is maintained during rapid state changes
     *
     * Tests that state remains consistent even when operations are performed
     * in rapid succession.
     */
    @Test
    fun testStateConsistencyDuringRapidChanges() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        val viewModel = GamepadViewModel(engine, this)

        // Rapid sequence of changes
        viewModel.setVibrationEnabled(true)
        viewModel.setPattern(VibrationPattern.Pulse(frequencyHz = 2.0f))
        viewModel.setIntensity(0.5f)
        advanceTimeBy(1)
        assertStateConsistency(viewModel, engine, "after rapid changes 1")

        viewModel.setPattern(VibrationPattern.Wave(frequencyHz = 1.0f))
        viewModel.setIntensity(0.8f)
        advanceTimeBy(1)
        assertStateConsistency(viewModel, engine, "after rapid changes 2")

        viewModel.setVibrationEnabled(false)
        viewModel.setPattern(VibrationPattern.Constant)
        viewModel.setIntensity(0.3f)
        advanceTimeBy(1)
        assertStateConsistency(viewModel, engine, "after rapid changes 3")
    }

    /**
     * Property: State consistency is maintained during connection state changes
     *
     * Tests that VibrationState remains consistent when connection state changes
     * occur.
     */
    @Test
    fun testStateConsistencyDuringConnectionChanges() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        val viewModel = GamepadViewModel(engine, this)

        // Enable vibration
        viewModel.setVibrationEnabled(true)
        viewModel.setPattern(VibrationPattern.Pulse(frequencyHz = 2.0f))
        viewModel.setIntensity(0.7f)
        advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 2)
        assertStateConsistency(viewModel, engine, "before connection change")

        // Simulate connection loss
        controller.setConnectionState(ConnectionState.Disconnected)
        advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 2)
        assertStateConsistency(viewModel, engine, "after disconnection")

        // Reconnect
        controller.setConnectionState(ConnectionState.Connected("Test Controller"))
        advanceTimeBy(1)
        assertStateConsistency(viewModel, engine, "after reconnection")
    }

    /**
     * Property: State consistency is maintained across enable/disable cycles
     *
     * Tests that state remains consistent through multiple enable/disable cycles.
     */
    @Test
    fun testStateConsistencyAcrossEnableDisableCycles() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        val viewModel = GamepadViewModel(engine, this)

        for (i in 1..5) {
            // Enable
            viewModel.setVibrationEnabled(true)
            advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 2)
            assertStateConsistency(viewModel, engine, "cycle $i: after enable")

            // Disable
            viewModel.setVibrationEnabled(false)
            advanceTimeBy(1)
            assertStateConsistency(viewModel, engine, "cycle $i: after disable")
        }
    }

    /**
     * Property: State consistency is maintained when changing patterns during vibration
     *
     * Tests that state remains consistent when patterns are changed while
     * vibration is active.
     */
    @Test
    fun testStateConsistencyWhenChangingPatternsDuringVibration() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        val viewModel = GamepadViewModel(engine, this)

        val patterns = listOf(
            VibrationPattern.Constant,
            VibrationPattern.Pulse(frequencyHz = 1.0f),
            VibrationPattern.Wave(frequencyHz = 0.5f),
            VibrationPattern.Pulse(frequencyHz = 3.0f),
            VibrationPattern.Wave(frequencyHz = 2.0f)
        )

        viewModel.setVibrationEnabled(true)
        advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 2)

        for (pattern in patterns) {
            viewModel.setPattern(pattern)
            advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 2)
            assertStateConsistency(viewModel, engine, "after changing to ${pattern::class.simpleName}")
        }

        // Clean up
        viewModel.setVibrationEnabled(false)
        advanceTimeBy(1)
    }

    /**
     * Property: State consistency is maintained when changing intensity during vibration
     *
     * Tests that state remains consistent when intensity is changed while
     * vibration is active.
     */
    @Test
    fun testStateConsistencyWhenChangingIntensityDuringVibration() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        val viewModel = GamepadViewModel(engine, this)

        val intensities = listOf(0.1f, 0.3f, 0.5f, 0.7f, 0.9f, 0.4f, 0.6f)

        viewModel.setVibrationEnabled(true)
        advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 2)

        for (intensity in intensities) {
            viewModel.setIntensity(intensity)
            advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 2)
            assertStateConsistency(viewModel, engine, "after changing intensity to $intensity")
        }

        // Clean up
        viewModel.setVibrationEnabled(false)
        advanceTimeBy(1)
    }

    /**
     * Property: State consistency is maintained with all state fields
     *
     * Tests that all fields in VibrationState (enabled, activePattern, intensity)
     * remain consistent across layers.
     */
    @Test
    fun testStateConsistencyForAllFields() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        val viewModel = GamepadViewModel(engine, this)

        val testCases = listOf(
            Triple(true,  VibrationPattern.Constant,              0.5f),
            Triple(false, VibrationPattern.Pulse(frequencyHz = 2.0f), 0.7f),
            Triple(true,  VibrationPattern.Wave(frequencyHz = 1.0f),  0.3f),
            Triple(false, VibrationPattern.Constant,              1.0f),
            Triple(true,  VibrationPattern.Pulse(frequencyHz = 3.0f), 0.0f)
        )

        for ((enabled, pattern, intensity) in testCases) {
            viewModel.setVibrationEnabled(enabled)
            viewModel.setPattern(pattern)
            viewModel.setIntensity(intensity)
            advanceTimeBy(VibrationEngine.FRAME_INTERVAL_MS * 2)

            val vm = viewModel.vibrationState.value
            val eng = engine.vibrationState.value
            assertEquals(eng.enabled, vm.enabled, "Enabled should be consistent: $enabled")
            assertEquals(eng.activePattern, vm.activePattern, "Pattern should be consistent")
            assertEquals(eng.intensity, vm.intensity, "Intensity should be consistent: $intensity")
        }

        // Clean up - last testCase has enabled=true, so disable
        viewModel.setVibrationEnabled(false)
        advanceTimeBy(1)
    }

    /**
     * Property: Initial state is consistent
     *
     * Tests that the initial state is consistent between ViewModel and Engine
     * immediately after creation.
     */
    @Test
    fun testInitialStateConsistency() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        val viewModel = GamepadViewModel(engine, this)

        advanceTimeBy(1) // Allow initialization

        assertStateConsistency(viewModel, engine, "initial state")
    }

    /**
     * Helper function to assert state consistency between ViewModel and Engine.
     */
    private fun assertStateConsistency(
        viewModel: GamepadViewModel,
        engine: VibrationEngine,
        context: String
    ) {
        val viewModelVibrationState = viewModel.vibrationState.value
        val engineVibrationState = engine.vibrationState.value

        assertEquals(
            engineVibrationState.enabled,
            viewModelVibrationState.enabled,
            "Enabled state should be consistent $context"
        )
        assertEquals(
            engineVibrationState.activePattern,
            viewModelVibrationState.activePattern,
            "Active pattern should be consistent $context"
        )
        assertEquals(
            engineVibrationState.intensity,
            viewModelVibrationState.intensity,
            "Intensity should be consistent $context"
        )

        val viewModelConnectionState = viewModel.connectionState.value
        val engineConnectionState = engine.controller.connectionState.value

        assertEquals(
            engineConnectionState,
            viewModelConnectionState,
            "Connection state should be consistent $context"
        )
    }
}
