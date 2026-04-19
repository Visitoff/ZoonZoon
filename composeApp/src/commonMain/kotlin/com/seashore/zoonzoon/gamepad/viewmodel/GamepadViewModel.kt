package com.seashore.zoonzoon.gamepad.viewmodel

import com.seashore.zoonzoon.gamepad.engine.VibrationEngine
import com.seashore.zoonzoon.gamepad.model.ConnectionState
import com.seashore.zoonzoon.gamepad.model.VibrationPattern
import com.seashore.zoonzoon.gamepad.model.VibrationState
import com.seashore.zoonzoon.gamepad.model.PatternManager
import com.seashore.zoonzoon.gamepad.ui.DisplayError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for gamepad vibration control.
 *
 * This ViewModel manages the UI state for controller connection and vibration status.
 * It exposes [ConnectionState] and [VibrationState] as [StateFlow] objects that the
 * UI layer can observe reactively.
 *
 * The ViewModel coordinates user actions (enable/disable vibration, pattern selection,
 * intensity adjustment) by delegating to the [VibrationEngine]. It also listens to
 * state changes from the engine and updates its own state accordingly.
 *
 * **Validates: Requirements 2.1, 2.2, 3.1, 4.1, 8.1, 8.2, 8.3, 8.4, 8.5, 10.1, 10.2, 10.3, 10.4, 10.5**
 *
 * @param vibrationEngine The [VibrationEngine] instance that handles vibration execution.
 * @param scope The [CoroutineScope] for launching coroutines. In production, this would
 *              typically be viewModelScope from AndroidX ViewModel.
 */
class GamepadViewModel(
    private val vibrationEngine: VibrationEngine,
    private val scope: CoroutineScope,
    private val patternManager: PatternManager = PatternManager()
) {

    /**
     * Observable connection state.
     * Emits updates when controllers connect or disconnect.
     *
     * **Validates: Requirement 8.1**
     */
    val connectionState: StateFlow<ConnectionState> = vibrationEngine.controller.connectionState

    /**
     * Observable vibration state.
     * Emits updates when vibration is enabled/disabled, pattern changes, or intensity changes.
     *
     * **Validates: Requirement 8.2**
     */
    val vibrationState: StateFlow<VibrationState> = vibrationEngine.vibrationState

    // Error state management
    private val _errorState = MutableStateFlow<DisplayError?>(null)
    
    /**
     * Observable error state.
     * Emits updates when errors occur or are cleared.
     *
     * **Validates: Requirements 10.1, 10.2, 10.3, 10.4, 10.5**
     */
    val errorState: StateFlow<DisplayError?> = _errorState.asStateFlow()

    init {
        // Listen to VibrationEngine state changes
        // The engine's vibrationState is already a StateFlow, so we can observe it directly
        // State updates are handled automatically through the StateFlow
        observeEngineStateChanges()
    }

    /**
     * Enable or disable vibration.
     *
     * When enabled, the currently selected pattern will start executing.
     * When disabled, vibration stops and a stop command (motor values 0) is sent.
     *
     * **Validates: Requirements 2.1, 2.2**
     *
     * @param enabled True to enable vibration, false to disable.
     */
    fun setVibrationEnabled(enabled: Boolean) {
        if (enabled) {
            vibrationEngine.enableVibration()
        } else {
            vibrationEngine.disableVibration()
        }
    }

    /**
     * Set the active vibration pattern.
     *
     * The new pattern takes effect immediately. If vibration is currently enabled,
     * the new pattern will start executing on the next frame.
     *
     * **Validates: Requirement 3.1**
     *
     * @param pattern The [VibrationPattern] to activate.
     */
    fun setPattern(pattern: VibrationPattern) {
        vibrationEngine.setPattern(pattern)
    }

    /**
     * Set the vibration intensity.
     *
     * The new intensity is applied to all subsequent vibration frames. If vibration
     * is currently active, the change takes effect on the next frame.
     *
     * **Validates: Requirement 4.1**
     *
     * @param intensity Intensity in the range [0.0, 1.0].
     * @throws IllegalArgumentException if [intensity] is outside [0.0, 1.0].
     */
    fun setIntensity(intensity: Float) {
        require(intensity in 0.0f..1.0f) {
            "Intensity must be in range [0.0, 1.0], got $intensity"
        }
        vibrationEngine.setIntensity(intensity)
    }

    /**
     * Save a custom pattern and optionally activate it.
     *
     * **Validates: Requirement 3.5**
     */
    fun saveAndApplyCustomPattern(
        name: String,
        frames: List<VibrationPattern.Custom.Frame>,
        loop: Boolean = true
    ) {
        val pattern = VibrationPattern.Custom(frames = frames, loop = loop)
        patternManager.removeCustomPattern(name)
        patternManager.registerCustomPattern(name, pattern)
        vibrationEngine.setPattern(pattern)
    }

    /**
     * Get all available patterns including custom ones.
     */
    fun getAllPatterns(): List<VibrationPattern> = patternManager.getAllPatterns()

    /**
     * Start controller discovery.
     * **Validates: Requirement 1.1**
     */
    fun startDiscovery() {
        scope.launch {
            try {
                vibrationEngine.controller.startDiscovery()
            } catch (e: Exception) {
                _errorState.value = com.seashore.zoonzoon.gamepad.ui.DisplayError(
                    message = "Failed to start discovery: ${e.message}",
                    severity = com.seashore.zoonzoon.gamepad.ui.ErrorSeverity.ERROR
                )
            }
        }
    }

    /**
     * Stop controller discovery.
     */
    fun stopDiscovery() {
        scope.launch {
            vibrationEngine.controller.stopDiscovery()
        }
    }

    /**
     * Disconnect from the current controller.
     * **Validates: Requirement 1.4**
     */
    fun disconnect() {
        scope.launch {
            vibrationEngine.disableVibration()
            vibrationEngine.controller.disconnect()
        }
    }

    /**
     * Observe state changes from the VibrationEngine.
     *
     * This method sets up observation of the engine's state. Since the engine
     * exposes its state as StateFlow, the ViewModel can directly expose these
     * flows to the UI layer. State updates propagate automatically.
     *
     * **Validates: Requirements 8.3, 8.4, 8.5**
     */
    private fun observeEngineStateChanges() {
        // The VibrationEngine already exposes StateFlow objects for both
        // connectionState (via controller) and vibrationState.
        // We expose these directly to the UI layer, so state updates
        // propagate automatically without additional observation logic.
        //
        // This design ensures state consistency between the ViewModel,
        // VibrationEngine, and UI_Layer as required by Requirement 8.5.
    }
}
