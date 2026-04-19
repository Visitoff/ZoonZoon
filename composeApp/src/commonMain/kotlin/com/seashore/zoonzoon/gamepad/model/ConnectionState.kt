package com.seashore.zoonzoon.gamepad.model

/**
 * Represents the current state of controller connectivity.
 * 
 * This sealed class models the three possible connection states:
 * - Disconnected: No controller is connected
 * - Scanning: Actively searching for controllers
 * - Connected: A controller is connected with a specific type
 */
sealed class ConnectionState {
    /**
     * No controller is currently connected.
     */
    data object Disconnected : ConnectionState()
    
    /**
     * The system is actively scanning for available controllers.
     */
    data object Scanning : ConnectionState()
    
    /**
     * A controller is connected.
     * 
     * @param controllerType The type of connected controller (e.g., "DualShock 4", "DualSense", "Xbox")
     */
    data class Connected(val controllerType: String) : ConnectionState()
}
