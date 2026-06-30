package com.seashore.zoonzoon.gamepad.platform

import GameControllerHaptics.GameControllerHaptics
import com.seashore.zoonzoon.gamepad.engine.GamepadControllerWithState
import com.seashore.zoonzoon.gamepad.model.ConnectionState
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.GameController.GCController
import platform.GameController.GCControllerDidBecomeCurrentNotification
import platform.GameController.GCControllerDidConnectNotification
import platform.GameController.GCControllerDidDisconnectNotification

/**
 * iOS implementation of PlatformGamepadController.
 *
 * Uses GameControllerHaptics ObjC bridge to send haptic commands
 * to the game controller via GCController.haptics + CoreHaptics.
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
    private var becomeCurrentObserver: Any? = null
    private var hapticsPrepared = false

    private val haptics = GameControllerHaptics()

    actual override suspend fun startDiscovery() = withContext(Dispatchers.Main) {
        _connectionState.value = ConnectionState.Scanning
        GCController.startWirelessControllerDiscovery(null)

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

        becomeCurrentObserver = NSNotificationCenter.defaultCenter.addObserverForName(
            name = GCControllerDidBecomeCurrentNotification,
            `object` = null,
            queue = NSOperationQueue.mainQueue
        ) { notification ->
            val controller = notification?.`object` as? GCController
            controller?.let { prepareHapticsIfNeeded(it) }
        }

        val controllers = GCController.controllers()
        if (controllers.isNotEmpty()) {
            val first = controllers.first() as? GCController
            first?.let { handleControllerConnected(it) }
        }
    }

    actual override suspend fun stopDiscovery() = withContext(Dispatchers.Main) {
        GCController.stopWirelessControllerDiscovery()
        connectObserver?.let { NSNotificationCenter.defaultCenter.removeObserver(it) }
        disconnectObserver?.let { NSNotificationCenter.defaultCenter.removeObserver(it) }
        becomeCurrentObserver?.let { NSNotificationCenter.defaultCenter.removeObserver(it) }
        connectObserver = null
        disconnectObserver = null
        becomeCurrentObserver = null

        if (_connectionState.value is ConnectionState.Scanning) {
            _connectionState.value = ConnectionState.Disconnected
        }
    }

    actual override suspend fun sendVibrationCommand(
        leftMotor: Float,
        rightMotor: Float
    ): Result<Unit> = withContext(Dispatchers.Main) {
        val controller = resolveActiveController()
            ?: return@withContext Result.failure(IllegalStateException("No controller connected"))

        if (leftMotor !in 0f..1f || rightMotor !in 0f..1f) {
            return@withContext Result.failure(IllegalArgumentException("Motor values must be in [0.0, 1.0]"))
        }

        if (!ensureHapticsPrepared(controller)) {
            return@withContext Result.failure(IllegalStateException("Controller does not support haptics"))
        }

        var ok = haptics.updateRumbleWithLeftIntensity(leftMotor, rightIntensity = rightMotor)
        if (!ok) {
            invalidateHaptics()
            if (ensureHapticsPrepared(controller)) {
                ok = haptics.updateRumbleWithLeftIntensity(leftMotor, rightIntensity = rightMotor)
            }
        }

        if (ok) Result.success(Unit)
        else Result.failure(IllegalStateException("Failed to update controller haptics"))
    }

    actual override suspend fun disconnect() = withContext(Dispatchers.Main) {
        GCController.stopWirelessControllerDiscovery()
        invalidateHaptics()
        connectedController = null
        _connectionState.value = ConnectionState.Disconnected
    }

    private fun handleControllerConnected(controller: GCController) {
        connectedController = controller
        val name = controller.vendorName ?: "Game Controller"
        prepareHapticsIfNeeded(controller)
        println("[iOS] Connected: $name, haptics=$hapticsPrepared")
        _connectionState.value = ConnectionState.Connected(name)
    }

    private fun handleControllerDisconnected(controller: GCController) {
        if (connectedController == controller) {
            invalidateHaptics()
            connectedController = null
            _connectionState.value = ConnectionState.Disconnected
        }
    }

    private fun prepareHapticsIfNeeded(controller: GCController) {
        connectedController = controller
        invalidateHaptics()
        hapticsPrepared = haptics.prepareForController(controller)
    }

    private fun resolveActiveController(): GCController? {
        val current = GCController.current
        if (current != null) {
            connectedController = current
            return current
        }
        return connectedController
    }

    private fun ensureHapticsPrepared(controller: GCController): Boolean {
        if (hapticsPrepared) return true
        hapticsPrepared = haptics.prepareForController(controller)
        return hapticsPrepared
    }

    private fun invalidateHaptics() {
        haptics.stopAll()
        hapticsPrepared = false
    }
}
