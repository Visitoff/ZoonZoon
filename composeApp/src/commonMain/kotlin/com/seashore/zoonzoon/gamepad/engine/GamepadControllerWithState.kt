package com.seashore.zoonzoon.gamepad.engine

import com.seashore.zoonzoon.gamepad.model.ConnectionState
import kotlinx.coroutines.flow.StateFlow

/**
 * Extended interface for gamepad controllers that expose connection state
 * and lifecycle management.
 *
 * Combines [GamepadController] with connection state observation and
 * discovery/disconnect lifecycle, allowing the VibrationEngine and
 * ViewModel to react to connection changes.
 */
interface GamepadControllerWithState : GamepadController {

    /** Observable connection state. Emits on connect/disconnect. */
    val connectionState: StateFlow<ConnectionState>

    /** Start scanning for available controllers. */
    suspend fun startDiscovery()

    /** Stop scanning for controllers. */
    suspend fun stopDiscovery()

    /** Disconnect from the current controller and clean up resources. */
    suspend fun disconnect()
}
