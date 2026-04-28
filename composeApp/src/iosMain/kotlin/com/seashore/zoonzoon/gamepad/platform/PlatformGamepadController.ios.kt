package com.seashore.zoonzoon.gamepad.platform

import com.seashore.zoonzoon.gamepad.engine.GamepadControllerWithState
import com.seashore.zoonzoon.gamepad.model.ConnectionState
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
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
import platform.Foundation.NSError
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.GameController.GCController
import platform.GameController.GCControllerDidConnectNotification
import platform.GameController.GCControllerDidDisconnectNotification
import platform.GameController.GCHapticsLocalityDefault

/**
 * iOS implementation of PlatformGamepadController.
 *
 * Uses GCController.haptics to create a CHHapticEngine bound to the
 * game controller (not the phone). This is the correct Apple approach
 * as shown in the "Playing Haptics on Game Controllers" sample.
 *
 * Works with DualShock 4, DualSense, Xbox controllers that support
 * GCController.haptics on iOS 14+.
 *
 * **Validates: Requirements 1.1, 1.2, 6.1, 6.3, 1.9, 10.4**
 */
@OptIn(ExperimentalForeignApi::class)
actual class PlatformGamepadController : GamepadControllerWithState {

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    actual override val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Main + Job())

    private var connectedController: GCController? = null
    private var connectObserver: Any? = null
    private var disconnectObserver: Any? = null

    // Engine map per locality — same pattern as Apple sample
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
     * Send a vibration command to the connected controller.
     *
     * Creates a CHHapticEngine via controller.haptics (bound to the gamepad,
     * not the phone) and plays a continuous haptic event with the given intensity.
     *
     * **Validates: Requirements 6.3, 6.4**
     */
    actual override suspend fun sendVibrationCommand(
        leftMotor: Float,
        rightMotor: Float
    ): Result<Unit> {
        val controller = connectedController
            ?: return Result.failure(IllegalStateException("No controller connected"))

        if (leftMotor !in 0f..1f || rightMotor !in 0f..1f) {
            return Result.failure(IllegalArgumentException("Motor values must be in [0.0, 1.0]"))
        }

        val intensity = maxOf(leftMotor, rightMotor).coerceIn(0f, 1f)

        if (intensity <= 0f) {
            hapticEngine?.stopWithCompletionHandler(null)
            return Result.success(Unit)
        }

        return try {
            // Get or create engine bound to the CONTROLLER (not phone)
            // This is the key: createEngine(withLocality:) on controller.haptics
            val engine = hapticEngine ?: run {
                val newEngine = controller.haptics?.createEngineWithLocality(
                    GCHapticsLocalityDefault
                ) ?: return Result.failure(
                    IllegalStateException(
                        "Controller '${controller.vendorName}' does not support GCController.haptics."
                    )
                )

                newEngine.stoppedHandler = { reason: platform.CoreHaptics.CHHapticEngine.StoppedReason ->
                    println("Haptic engine stopped: $reason")
                }
                newEngine.resetHandler = {
                    println("Haptic engine reset — restarting")
                    newEngine.startWithCompletionHandler(null)
                }

                hapticEngine = newEngine
                newEngine
            }

            // Start engine
            engine.startWithCompletionHandler(null)

            // Build a short continuous haptic event with the given intensity
            memScoped {
                val errorPtr = alloc<ObjCObjectVar<NSError?>>()

                val intensityParam = CHHapticEventParameter(
                    parameterID = "HapticIntensity",
                    value = intensity
                )
                val sharpnessParam = CHHapticEventParameter(
                    parameterID = "HapticSharpness",
                    value = 0.1f  // Low sharpness = rumble feel
                )

                val event = CHHapticEvent(
                    eventType = CHHapticEventTypeHapticContinuous,
                    parameters = listOf(intensityParam, sharpnessParam),
                    relativeTime = 0.0,
                    duration = 0.1  // 100ms per frame
                )

                val pattern = CHHapticPattern(
                    events = listOf(event),
                    parameters = emptyList<CHHapticEventParameter>(),
                    error = errorPtr.ptr
                ) ?: return Result.failure(IllegalStateException("Failed to create haptic pattern"))

                val player = engine.createPlayerWithPattern(pattern, error = errorPtr.ptr)
                    ?: return Result.failure(IllegalStateException("Failed to create haptic player"))

                player.startAtTime(0.0, error = errorPtr.ptr)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(IllegalStateException("Haptic error: ${e.message}", e))
        }
    }

    actual override suspend fun disconnect() {
        hapticEngine?.stopWithCompletionHandler(null)
        hapticEngine = null
        connectedController = null
        _connectionState.value = ConnectionState.Disconnected
    }

    private fun handleControllerConnected(controller: GCController) {
        connectedController = controller
        hapticEngine = null  // Reset engine for new controller
        val name = controller.vendorName ?: "Game Controller"
        println("Connected: $name (haptics supported: ${controller.haptics != null})")
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
