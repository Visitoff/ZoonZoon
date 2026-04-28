package com.seashore.zoonzoon.gamepad.platform

import com.seashore.zoonzoon.gamepad.engine.GamepadControllerWithState
import com.seashore.zoonzoon.gamepad.model.ConnectionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.GameController.GCController
import platform.GameController.GCControllerDidConnectNotification
import platform.GameController.GCControllerDidDisconnectNotification
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue

/**
 * iOS implementation of PlatformGamepadController.
 *
 * Uses the iOS GameController Framework to communicate with MFi-certified
 * controllers. Vibration is sent via GCController.haptics API (iOS 14+).
 *
 * NOTE: Most MFi controllers do NOT support haptics via GCController.haptics.
 * DualShock 4 and Xbox controllers connected via Bluetooth on iOS do not
 * expose haptic motors through public Apple APIs.
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
     * Uses GCController.haptics API. Only works with MFi controllers that
     * explicitly support haptic feedback (e.g. some Xbox controllers on iOS 14+).
     * DualShock 4 / DualSense over Bluetooth do NOT support this on iOS.
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

        return try {
            val haptics = controller.haptics
                ?: return Result.failure(
                    IllegalStateException(
                        "Controller '${controller.vendorName}' does not support haptics via GCController.haptics. " +
                        "This is expected for DualShock 4 and Xbox controllers on iOS."
                    )
                )

            // Use GCHapticsLocality to target controller motors
            val leftEngine = haptics.createEngineWithLocality("GCHapticsLocalityLeftHandle")
            val rightEngine = haptics.createEngineWithLocality("GCHapticsLocalityRightHandle")

            if (leftMotor > 0f) leftEngine?.startAndReturnError(null)
            if (rightMotor > 0f) rightEngine?.startAndReturnError(null)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(IllegalStateException("iOS haptic error: ${e.message}", e))
        }
    }

    actual override suspend fun disconnect() {
        connectedController = null
        _connectionState.value = ConnectionState.Disconnected
    }

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
