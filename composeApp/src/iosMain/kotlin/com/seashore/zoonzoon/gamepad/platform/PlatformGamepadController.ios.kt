package com.seashore.zoonzoon.gamepad.platform

import com.seashore.zoonzoon.gamepad.engine.GamepadControllerWithState
import com.seashore.zoonzoon.gamepad.model.ConnectionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.CoreHaptics.CHHapticEngine
import platform.CoreHaptics.CHHapticEvent
import platform.CoreHaptics.CHHapticEventParameter
import platform.CoreHaptics.CHHapticEventTypeHapticContinuous
import platform.CoreHaptics.CHHapticPattern
import platform.GameController.GCController
import platform.GameController.GCControllerDidConnectNotification
import platform.GameController.GCControllerDidDisconnectNotification
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue

// CoreHaptics parameter ID constants (string-based in Kotlin/Native)
private const val kHapticIntensity = "HapticIntensity"
private const val kHapticSharpness = "HapticSharpness"

/**
 * iOS implementation of PlatformGamepadController.
 *
 * Uses the iOS GameController Framework + CoreHaptics to communicate with
 * MFi-certified controllers. Non-MFi controllers are not supported on iOS.
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
    private var hapticEngine: CHHapticEngine? = null

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

        val controllers = GCController.controllers()
        if (controllers.isNotEmpty()) {
            val first = controllers.first() as? GCController
            first?.let { handleControllerConnected(it) }
        }
    }

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
     * Uses CoreHaptics to play a continuous haptic event with the given intensity.
     * This is the closest public iOS API equivalent to gamepad rumble.
     *
     * **Validates: Requirements 6.3, 6.4**
     */
    actual override suspend fun sendVibrationCommand(
        leftMotor: Float,
        rightMotor: Float
    ): Result<Unit> {
        connectedController
            ?: return Result.failure(IllegalStateException("No MFi controller connected"))

        if (leftMotor !in 0f..1f || rightMotor !in 0f..1f) {
            return Result.failure(IllegalArgumentException("Motor values must be in [0.0, 1.0]"))
        }

        val intensity = maxOf(leftMotor, rightMotor).coerceIn(0f, 1f)

        return try {
            val engine = getOrCreateHapticEngine()
                ?: return Result.failure(IllegalStateException("Failed to create haptic engine"))

            if (intensity <= 0f) {
                // Stop any ongoing haptics
                engine.stopWithCompletionHandler(null)
                return Result.success(Unit)
            }

            // Build a short continuous haptic event
            val intensityParam = CHHapticEventParameter(
                parameterID = kHapticIntensity,
                value = intensity
            )
            val sharpnessParam = CHHapticEventParameter(
                parameterID = kHapticSharpness,
                value = 0.5f
            )

            val event = CHHapticEvent(
                eventType = CHHapticEventTypeHapticContinuous,
                parameters = listOf(intensityParam, sharpnessParam),
                relativeTime = 0.0,
                duration = 0.1  // 100ms pulse per frame
            )

            val pattern = CHHapticPattern(
                events = listOf(event),
                parameters = emptyList<CHHapticEventParameter>(),
                error = null
            ) ?: return Result.failure(IllegalStateException("Failed to create haptic pattern"))

            val player = engine.createPlayerWithPattern(pattern, error = null)
                ?: return Result.failure(IllegalStateException("Failed to create haptic player"))

            player.startAtTime(0.0, error = null)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(IllegalStateException("iOS haptic error: ${e.message}", e))
        }
    }

    actual override suspend fun disconnect() {
        hapticEngine?.stopWithCompletionHandler(null)
        hapticEngine = null
        connectedController = null
        _connectionState.value = ConnectionState.Disconnected
    }

    private fun getOrCreateHapticEngine(): CHHapticEngine? {
        hapticEngine?.let { return it }
        val engine = CHHapticEngine(error = null) ?: return null
        engine.startAndReturnError(null)
        hapticEngine = engine
        return engine
    }

    private fun handleControllerConnected(controller: GCController) {
        connectedController = controller
        val name = controller.vendorName ?: "MFi Controller"
        _connectionState.value = ConnectionState.Connected(name)
    }

    private fun handleControllerDisconnected(controller: GCController) {
        if (connectedController == controller) {
            hapticEngine?.stopWithCompletionHandler(null)
            hapticEngine = null
            connectedController = null
            _connectionState.value = ConnectionState.Disconnected
        }
    }
}
