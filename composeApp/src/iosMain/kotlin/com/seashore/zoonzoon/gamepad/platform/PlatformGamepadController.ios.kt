package com.seashore.zoonzoon.gamepad.platform

import GameControllerHaptics.GameControllerHaptics
import com.seashore.zoonzoon.gamepad.engine.GamepadControllerWithState
import com.seashore.zoonzoon.gamepad.model.ConnectionState
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
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

    private var connectedController: GCController? = null
    private var connectObserver: Any? = null
    private var disconnectObserver: Any? = null
    private var becomeCurrentObserver: Any? = null
    private var hapticsPrepared = false
    private var hapticSharpness = 0.5f

    private val haptics = GameControllerHaptics()

    actual override suspend fun startDiscovery() {
        withContext(Dispatchers.Main) {
            _connectionState.value = ConnectionState.Scanning
            GameControllerHaptics.startWirelessDiscovery()

            if (connectObserver == null) {
                connectObserver = NSNotificationCenter.defaultCenter.addObserverForName(
                    name = GCControllerDidConnectNotification,
                    `object` = null,
                    queue = NSOperationQueue.mainQueue
                ) { notification ->
                    val controller = notification?.`object` as? GCController
                    controller?.let { handleControllerConnected(it) }
                }
            }

            if (disconnectObserver == null) {
                disconnectObserver = NSNotificationCenter.defaultCenter.addObserverForName(
                    name = GCControllerDidDisconnectNotification,
                    `object` = null,
                    queue = NSOperationQueue.mainQueue
                ) { notification ->
                    val controller = notification?.`object` as? GCController
                    controller?.let { handleControllerDisconnected(it) }
                }
            }

            if (becomeCurrentObserver == null) {
                becomeCurrentObserver = NSNotificationCenter.defaultCenter.addObserverForName(
                    name = GCControllerDidBecomeCurrentNotification,
                    `object` = null,
                    queue = NSOperationQueue.mainQueue
                ) { notification ->
                    val controller = notification?.`object` as? GCController
                    controller?.let { handleControllerConnected(it) }
                }
            }

            // Prefer the actively used controller, then any already-connected one.
            val preferred = GCController.current
                ?: (GCController.controllers().firstOrNull() as? GCController)
            if (preferred != null) {
                handleControllerConnected(preferred)
            }
        }
    }

    actual override suspend fun stopDiscovery() {
        withContext(Dispatchers.Main) {
            GameControllerHaptics.stopWirelessDiscovery()
            // Keep observers — controller may reconnect while app stays open.
            // Only leave Scanning if we never connected.
            if (_connectionState.value is ConnectionState.Scanning) {
                _connectionState.value = ConnectionState.Disconnected
            }
        }
    }

    actual override suspend fun sendVibrationCommand(
        leftMotor: Float,
        rightMotor: Float
    ): Result<Unit> = withContext(Dispatchers.Main) {
        if (leftMotor !in 0f..1f || rightMotor !in 0f..1f) {
            return@withContext Result.failure(IllegalArgumentException("Motor values must be in [0.0, 1.0]"))
        }

        val controller = resolveActiveController()
            ?: return@withContext Result.failure(IllegalStateException("No controller connected"))

        if (!ensureHapticsPrepared(controller)) {
            println("[iOS] Haptics prepare failed for ${controller.vendorName}")
            return@withContext Result.failure(IllegalStateException("Controller does not support haptics"))
        }

        var ok = haptics.updateRumbleWithLeftIntensity(
            leftMotor,
            rightIntensity = rightMotor,
            sharpness = hapticSharpness
        )
        if (!ok) {
            // Engine may have been stopped by the system — full re-prepare once.
            invalidateHaptics()
            if (ensureHapticsPrepared(controller)) {
                ok = haptics.updateRumbleWithLeftIntensity(
                    leftMotor,
                    rightIntensity = rightMotor,
                    sharpness = hapticSharpness
                )
            }
        }

        if (ok) Result.success(Unit)
        else {
            println("[iOS] Rumble update failed left=$leftMotor right=$rightMotor")
            Result.failure(IllegalStateException("Failed to update controller haptics"))
        }
    }

    actual override suspend fun disconnect() {
        withContext(Dispatchers.Main) {
            GameControllerHaptics.stopWirelessDiscovery()
            invalidateHaptics()
            connectedController = null
            _connectionState.value = ConnectionState.Disconnected
        }
    }

    private fun handleControllerConnected(controller: GCController) {
        connectedController = controller
        val name = controller.vendorName ?: "Game Controller"
        hapticsPrepared = haptics.prepareForController(controller)
        println("[iOS] Connected: $name, haptics=$hapticsPrepared, current=${GCController.current?.vendorName}")
        _connectionState.value = ConnectionState.Connected(name)
    }

    private fun handleControllerDisconnected(controller: GCController) {
        if (connectedController == controller) {
            invalidateHaptics()
            connectedController = null
            val remaining = GCController.controllers().firstOrNull() as? GCController
            if (remaining != null) {
                handleControllerConnected(remaining)
            } else {
                _connectionState.value = ConnectionState.Disconnected
            }
        }
    }

    private fun resolveActiveController(): GCController? {
        val current = GCController.current
        if (current != null) {
            if (connectedController !== current) {
                handleControllerConnected(current)
            }
            return current
        }

        connectedController?.let { return it }

        val first = GCController.controllers().firstOrNull() as? GCController
        if (first != null) {
            handleControllerConnected(first)
            return first
        }
        return null
    }

    private fun ensureHapticsPrepared(controller: GCController): Boolean {
        if (hapticsPrepared && connectedController === controller) return true
        hapticsPrepared = haptics.prepareForController(controller)
        connectedController = controller
        return hapticsPrepared
    }

    private fun invalidateHaptics() {
        haptics.stopAll()
        hapticsPrepared = false
    }

    override fun setHapticSharpness(sharpness: Float) {
        hapticSharpness = sharpness.coerceIn(0f, 1f)
    }
}
