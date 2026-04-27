package com.seashore.zoonzoon.gamepad.platform

import com.seashore.zoonzoon.gamepad.engine.GamepadControllerWithState
import com.seashore.zoonzoon.gamepad.model.ConnectionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.cinterop.ExperimentalForeignApi
import platform.GameController.GCController
import platform.GameController.GCControllerDidConnectNotification
import platform.GameController.GCControllerDidDisconnectNotification
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.CoreHaptics.CHHapticEngine
import platform.CoreHaptics.CHHapticEvent
import platform.CoreHaptics.CHHapticEventParameter
import platform.CoreHaptics.CHHapticEventTypeHapticContinuous
import platform.CoreHaptics.CHHapticParameterIDHapticIntensity
import platform.CoreHaptics.CHHapticParameterIDHapticSharpness
import platform.CoreHaptics.CHHapticPattern

/**
 * iOS implementation of PlatformGamepadController.
 *
 * Uses the iOS GameController Framework to communicate with MFi-certified
 * controllers. Non-MFi controllers are not supported on iOS (Apple restriction).
 *
 * **Validates: Requirements 1.1, 1.2, 6.1, 6.3, 1.9, 10.4**
 */
actual class PlatformGamepadController : GamepadControllerWithState {

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    actual override val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Main + Job())

    private var connectedController: GCController? = null
    private var connectObserver: Any? = null
    private var disconnectObserver: Any? = null

    // Controller haptics (CoreHaptics) state. We keep a strong reference so the engine doesn't get deallocated.
    private var controllerHapticsEngine: CHHapticEngine? = null

    /**
     * Start scanning for MFi controllers.
     *
     * Registers for GameController Framework connection notifications and
     * checks for already-connected controllers.
     *
     * **Validates: Requirements 1.1, 1.2, 1.9**
     */
    actual override suspend fun startDiscovery() {
        _connectionState.value = ConnectionState.Scanning

        connectObserver = NSNotificationCenter.defaultCenter.addObserverForName(
            name = GCControllerDidConnectNotification,
            `object` = null,
            queue = NSOperationQueue.mainQueue
        ) { notification ->
            val controller = notification?.`object` as? GCController
            controller?.let { handleControllerConnected(it) }
        }

        disconnectObserver = NSNotificationCenter.defaultCenter.addObserverForName(
            name = GCControllerDidDisconnectNotification,
            `object` = null,
            queue = NSOperationQueue.mainQueue
        ) { notification ->
            val controller = notification?.`object` as? GCController
            controller?.let { handleControllerDisconnected(it) }
        }

        // Check for already-connected controllers
        val controllers = GCController.controllers()
        if (controllers.isNotEmpty()) {
            val first = controllers.first() as? GCController
            first?.let { handleControllerConnected(it) }
        }
    }

    /**
     * Stop scanning for controllers.
     */
    actual override suspend fun stopDiscovery() {
        connectObserver?.let { NSNotificationCenter.defaultCenter.removeObserver(it) }
        disconnectObserver?.let { NSNotificationCenter.defaultCenter.removeObserver(it) }
        connectObserver = null
        disconnectObserver = null

        if (_connectionState.value is ConnectionState.Scanning) {
            _connectionState.value = ConnectionState.Disconnected
        }
    }

    /**
     * Send a vibration command to the connected MFi controller.
     *
     * Maps left/right motor values to iOS haptic engine APIs.
     *
     * **Validates: Requirements 6.3, 6.4**
     */
    actual override suspend fun sendVibrationCommand(
        leftMotor: Float,
        rightMotor: Float
    ): Result<Unit> {
        val controller = connectedController
            ?: return Result.failure(IllegalStateException("No MFi controller connected"))

        if (leftMotor !in 0f..1f || rightMotor !in 0f..1f) {
            return Result.failure(IllegalArgumentException("Motor values must be in [0.0, 1.0]"))
        }

        @OptIn(ExperimentalForeignApi::class)
        return try {
            val haptics = controller.haptics
            if (haptics == null) {
                return Result.failure(
                    IllegalStateException(
                        "This controller does not expose haptics via iOS GameController API (GCController.haptics is null). " +
                            "DualShock 4 over Bluetooth commonly has no controller haptics on iOS."
                    )
                )
            }

            // Create (or reuse) a CoreHaptics engine backed by the controller haptics.
            // On Apple platforms, `createEngineWithLocality("handles")` maps to the controller’s primary actuators.
            val engine = controllerHapticsEngine
                ?: (haptics.createEngineWithLocality("handles") as? CHHapticEngine)
                ?: return Result.failure(IllegalStateException("Failed to create CoreHaptics engine for controller"))

            controllerHapticsEngine = engine

            // Start engine (idempotent; OK to call repeatedly).
            engine.startAndReturnError(null)

            val intensity = maxOf(leftMotor, rightMotor).coerceIn(0f, 1f)
            if (intensity <= 0f) return Result.success(Unit)

            // Play a short continuous haptic pulse. This is the closest analogue to "rumble"
            // available through public iOS APIs when controller haptics are supported.
            val params = listOf(
                CHHapticEventParameter(parameterID = CHHapticParameterIDHapticIntensity, value = intensity),
                CHHapticEventParameter(parameterID = CHHapticParameterIDHapticSharpness, value = 0.5f)
            )
            val event = CHHapticEvent(
                eventType = CHHapticEventTypeHapticContinuous,
                parameters = params,
                relativeTime = 0.0,
                duration = 0.08
            )

            val pattern = CHHapticPattern(events = listOf(event), parameters = emptyList(), error = null)
                ?: return Result.failure(IllegalStateException("Failed to build CoreHaptics pattern"))

            val player = engine.createPlayerWithPattern(pattern, error = null)
                ?: return Result.failure(IllegalStateException("Failed to create CoreHaptics player"))

            player.startAtTime(0.0, error = null)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(IllegalStateException("iOS haptic error: ${e.message}", e))
        }
    }

    /**
     * Disconnect from the current controller.
     *
     * **Validates: Requirement 10.4** — MFi-only restriction handling
     */
    actual override suspend fun disconnect() {
        connectedController = null
        _connectionState.value = ConnectionState.Disconnected
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private fun handleControllerConnected(controller: GCController) {
        connectedController = controller
        val name = controller.vendorName ?: "MFi Controller"
        _connectionState.value = ConnectionState.Connected(name)
    }

    private fun handleControllerDisconnected(controller: GCController) {
        if (connectedController == controller) {
            connectedController = null
            _connectionState.value = ConnectionState.Disconnected
        }
    }
}
