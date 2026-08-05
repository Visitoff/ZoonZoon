package com.seashore.zoonzoon.gamepad.engine

import com.seashore.zoonzoon.gamepad.model.ConnectionState
import com.seashore.zoonzoon.gamepad.model.VibrationPattern
import com.seashore.zoonzoon.gamepad.model.VibrationState
import com.seashore.zoonzoon.gamepad.platform.PhoneVibrator
import com.seashore.zoonzoon.gamepad.platform.NoOpPhoneVibrator
import com.seashore.zoonzoon.settings.VibrationTarget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Core vibration orchestration component.
 *
 * VibrationEngine manages the vibration lifecycle: it tracks enabled/disabled state,
 * the active pattern, and the current intensity. When vibration is enabled it runs a
 * coroutine-based loop (~16 ms intervals, ~60 fps) that calculates pattern frames and
 * forwards motor values to the controller.
 *
 * State is exposed as a [StateFlow] of [VibrationState] so that higher-level components
 * (e.g. ViewModel) can observe changes reactively.
 *
 * The engine also observes the controller's connection state. When a connection is lost
 * during active vibration, the engine automatically stops vibration execution.
 *
 * **Validates: Requirements 1.2, 1.3, 1.4, 2.1, 2.2, 2.3, 2.4, 3.1, 4.1, 4.3, 5.1, 5.2, 5.4, 10.2**
 *
 * @param controller The gamepad controller used to send vibration commands and
 *                   observe connection state. In tests a fake can be supplied.
 * @param scope      The [CoroutineScope] in which the pattern execution loop and connection
 *                   state observation run. Callers are responsible for cancelling this scope
 *                   when the engine is no longer needed.
 */
class VibrationEngine(
    val controller: GamepadControllerWithState,
    private val scope: CoroutineScope,
    private val phoneVibrator: PhoneVibrator = NoOpPhoneVibrator,
    private val vibrationTarget: StateFlow<VibrationTarget>? = null
) {
    companion object {
        /** Target frame interval in milliseconds (~60 fps). */
        const val FRAME_INTERVAL_MS = 16L
    }

    // -------------------------------------------------------------------------
    // Internal mutable state
    // -------------------------------------------------------------------------

    private val _vibrationState = MutableStateFlow(VibrationState())

    /**
     * Observable vibration state. Emits whenever enabled status, active pattern,
     * or intensity changes.
     */
    val vibrationState: StateFlow<VibrationState> = _vibrationState.asStateFlow()

    /** Job for the currently running pattern execution loop, if any. */
    private var executionJob: Job? = null

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Enable vibration.
     *
     * Updates [VibrationState.enabled] to `true` and starts the pattern execution
     * loop if it is not already running.
     *
     * **Validates: Requirement 2.1, 2.4**
     */
    fun enableVibration() {
        _vibrationState.value = _vibrationState.value.copy(enabled = true)
        startExecutionLoop()
    }

    /**
     * Disable vibration.
     *
     * Stops the pattern execution loop, sends a stop command (motor values 0) to
     * the controller, and updates [VibrationState.enabled] to `false`.
     *
     * **Validates: Requirements 2.2, 2.3**
     */
    fun disableVibration() {
        stopExecutionLoop()
        _vibrationState.value = _vibrationState.value.copy(enabled = false)
        scope.launch {
            controller.sendVibrationCommand(leftMotor = 0f, rightMotor = 0f)
            phoneVibrator.stop()
        }
    }

    /**
     * Set the active vibration pattern.
     *
     * The new pattern takes effect immediately on the next frame. Intensity is
     * preserved across pattern changes.
     *
     * **Validates: Requirements 3.1, 4.4**
     *
     * @param pattern The [VibrationPattern] to activate.
     */
    fun setPattern(pattern: VibrationPattern) {
        _vibrationState.value = _vibrationState.value.copy(activePattern = pattern)
    }

    /**
     * Set the vibration intensity.
     *
     * The new intensity is applied to all subsequent frame calculations. If
     * vibration is currently active the change takes effect on the next frame.
     *
     * **Validates: Requirements 4.1, 4.3**
     *
     * @param intensity Intensity in the range [0.0, 1.0].
     * @throws IllegalArgumentException if [intensity] is outside [0.0, 1.0].
     */
    fun setIntensity(intensity: Float) {
        require(intensity in 0.0f..1.0f) {
            "Intensity must be in range [0.0, 1.0], got $intensity"
        }
        _vibrationState.value = _vibrationState.value.copy(intensity = intensity)
    }

    /**
     * Set haptic sharpness (Core Haptics character). Range [0.0, 1.0].
     * Applied on the next vibration frame on platforms that support it.
     */
    fun setSharpness(sharpness: Float) {
        require(sharpness in 0.0f..1.0f) {
            "Sharpness must be in range [0.0, 1.0], got $sharpness"
        }
        _vibrationState.value = _vibrationState.value.copy(sharpness = sharpness)
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    /**
     * Starts the coroutine-based pattern execution loop.
     *
     * The loop runs at approximately [FRAME_INTERVAL_MS] ms per iteration. Each
     * iteration reads the current state snapshot (pattern + intensity), calculates
     * the motor values for the elapsed time, and sends a vibration command to the
     * controller.
     *
     * If the loop is already running this is a no-op.
     *
     * The loop will stop if:
     * - Vibration is disabled
     * - A vibration command fails (indicating connection loss)
     * - The controller connection state becomes Disconnected
     *
     * **Validates: Requirements 5.1, 5.2, 5.4, 10.2**
     */
    private fun startExecutionLoop() {
        if (executionJob?.isActive == true) return

        executionJob = scope.launch {
            val startTime = currentTimeMillis()
            while (isActive) {
                val state = _vibrationState.value
                if (!state.enabled) break

                val connected = controller.connectionState.value is ConnectionState.Connected
                val target = vibrationTarget?.value ?: VibrationTarget.GAMEPAD_ONLY
                val usePhone = phoneVibrator.isAvailable && when (target) {
                    VibrationTarget.PHONE_ONLY -> true
                    VibrationTarget.GAMEPAD_AND_PHONE -> true
                    VibrationTarget.GAMEPAD_ONLY -> !connected
                }
                val useGamepad = connected && target != VibrationTarget.PHONE_ONLY

                if (!usePhone && !useGamepad) {
                    _vibrationState.value = _vibrationState.value.copy(enabled = false)
                    break
                }

                val elapsed = currentTimeMillis() - startTime
                val (left, right) = state.activePattern.calculateFrame(elapsed, state.intensity)
                val motorLevel = maxOf(left, right)

                if (useGamepad) {
                    controller.sendVibrationCommand(
                        leftMotor = left,
                        rightMotor = right,
                        sharpness = state.sharpness
                    )
                }

                if (usePhone) {
                    phoneVibrator.vibrate(motorLevel)
                }

                delay(FRAME_INTERVAL_MS)
            }
            phoneVibrator.stop()
        }
    }

    /**
     * Cancels the pattern execution loop if it is running.
     */
    private fun stopExecutionLoop() {
        executionJob?.cancel()
        executionJob = null
    }
}

/**
 * Returns the current wall-clock time in milliseconds.
 *
 * Extracted as a top-level function so it can be overridden in tests via
 * dependency injection if needed. The default implementation delegates to
 * [kotlinx.datetime] or the platform clock via [System.currentTimeMillis] on
 * JVM/Android and the equivalent on other targets.
 */
internal expect fun currentTimeMillis(): Long
