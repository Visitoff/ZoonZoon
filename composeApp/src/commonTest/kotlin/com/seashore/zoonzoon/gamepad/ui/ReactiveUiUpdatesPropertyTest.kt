package com.seashore.zoonzoon.gamepad.ui

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
 * Property tests for reactive UI updates.
 *
 * **Property 16: Reactive UI Updates**
 * **Validates: Requirement 7.9**
 *
 * Tests that when ViewModel state changes, the UI layer (via StateFlow)
 * reactively reflects the new state.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ReactiveUiUpdatesPropertyTest {

    @Test
    fun testVibrationStateUpdatesReactively() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        val viewModel = GamepadViewModel(engine, this)

        // Initial state
        assertFalse(viewModel.vibrationState.value.enabled)

        // Enable — UI should see the update immediately
        viewModel.setVibrationEnabled(true)
        assertTrue(viewModel.vibrationState.value.enabled, "UI should see enabled=true immediately")

        // Disable — UI should see the update immediately
        viewModel.setVibrationEnabled(false)
        advanceTimeBy(1)
        assertFalse(viewModel.vibrationState.value.enabled, "UI should see enabled=false immediately")
    }

    @Test
    fun testPatternChangeUpdatesReactively() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        val viewModel = GamepadViewModel(engine, this)

        val patterns = listOf(
            VibrationPattern.Constant,
            VibrationPattern.Pulse(2.0f),
            VibrationPattern.Wave(1.0f),
            VibrationPattern.Custom(emptyList())
        )

        for (pattern in patterns) {
            viewModel.setPattern(pattern)
            assertEquals(
                pattern,
                viewModel.vibrationState.value.activePattern,
                "UI should see pattern change to ${pattern::class.simpleName} immediately"
            )
        }
    }

    @Test
    fun testIntensityChangeUpdatesReactively() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        val viewModel = GamepadViewModel(engine, this)

        val intensities = listOf(0.0f, 0.25f, 0.5f, 0.75f, 1.0f)

        for (intensity in intensities) {
            viewModel.setIntensity(intensity)
            assertEquals(
                intensity,
                viewModel.vibrationState.value.intensity,
                "UI should see intensity=$intensity immediately"
            )
        }
    }

    @Test
    fun testConnectionStateUpdatesReactively() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        val viewModel = GamepadViewModel(engine, this)

        val states = listOf(
            ConnectionState.Disconnected,
            ConnectionState.Scanning,
            ConnectionState.Connected("DualShock 4"),
            ConnectionState.Connected("DualSense"),
            ConnectionState.Disconnected
        )

        for (state in states) {
            controller.setConnectionState(state)
            advanceTimeBy(1)
            assertEquals(
                state,
                viewModel.connectionState.value,
                "UI should see connection state change to $state"
            )
        }
    }

    @Test
    fun testMultipleRapidUpdatesAllReflected() = runTest {
        val controller = FakePlatformGamepadController()
        val engine = VibrationEngine(controller, this)
        val viewModel = GamepadViewModel(engine, this)

        // Rapid sequence of changes
        viewModel.setPattern(VibrationPattern.Pulse(2.0f))
        viewModel.setIntensity(0.3f)
        viewModel.setVibrationEnabled(true)
        advanceTimeBy(1)

        assertEquals(VibrationPattern.Pulse(2.0f), viewModel.vibrationState.value.activePattern)
        assertEquals(0.3f, viewModel.vibrationState.value.intensity)
        assertTrue(viewModel.vibrationState.value.enabled)

        // Clean up
        viewModel.setVibrationEnabled(false)
        advanceTimeBy(1)
    }
}
