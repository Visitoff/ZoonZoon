package com.seashore.zoonzoon.gamepad.platform

import com.seashore.zoonzoon.gamepad.engine.GamepadControllerWithState
import com.seashore.zoonzoon.gamepad.model.ConnectionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.GameController.GCController
import platform.GameController.GCControllerDidConnectNotification
import platform.GameController.GCControllerDidDisconnectNotification
import GameControllerHaptics.GameControllerHaptics

// Locality constants matching GCHapticsLocality raw values
private const val LOCALITY_DEFAULT = "GCHapticsLocalityDefault"
private const val LOCALITY_LEFT    = "GCHapticsLocalityLeftHandle"
private const val LOCALITY_RIGHT   = "GCHapticsLocalityRightHandle"

/**
 * iOS implementation of PlatformGamepadController.
 *
 * Uses GameControllerHaptics Swift bridge to send haptic commands
 * to the game controller via GCController.haptics + CoreHaptics.
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

    // Swift bridge for haptics
    private val haptics = GameControllerHaptics()

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

    actual override suspend fun sendVibrationCommand(
        leftMotor: Float,
        rightMotor: Float
    ): Result<Unit> {
        connectedController
            ?: return Result.failure(IllegalStateException("No controller connected"))

        if (leftMotor !in 0f..1f || rightMotor !in 0f..1f) {
            return Result.failure(IllegalArgumentException("Motor values must be in [0.0, 1.0]"))
        }

        if (leftMotor <= 0f && rightMotor <= 0f) {
            haptics.stopAll()
            return Result.success(Unit)
        }

        // Play rumble on left and/or right handle
        val leftOk  = if (leftMotor  > 0f) haptics.playRumble(leftMotor,  LOCALITY_LEFT)  else true
        val rightOk = if (rightMotor > 0f) haptics.playRumble(rightMotor, LOCALITY_RIGHT) else true

        // Fallback to default locality if both handles failed
        val ok = leftOk || rightOk || haptics.playRumble(maxOf(leftMotor, rightMotor), LOCALITY_DEFAULT)

        return if (ok) Result.success(Unit)
        else Result.failure(IllegalStateException("Controller does not support haptics"))
    }

    actual override suspend fun disconnect() {
        haptics.stopAll()
        connectedController = null
        _connectionState.value = ConnectionState.Disconnected
    }

    private fun handleControllerConnected(controller: GCController) {
        connectedController = controller
        val name = controller.vendorName ?: "Game Controller"

        // Pre-create engines for all localities
        haptics.prepareEngine(controller, LOCALITY_DEFAULT)
        haptics.prepareEngine(controller, LOCALITY_LEFT)
        haptics.prepareEngine(controller, LOCALITY_RIGHT)

        println("[iOS] Connected: $name")
        _connectionState.value = ConnectionState.Connected(name)
    }

    private fun handleControllerDisconnected(controller: GCController) {
        if (connectedController == controller) {
            haptics.stopAll()
            connectedController = null
            _connectionState.value = ConnectionState.Disconnected
        }
    }
}
