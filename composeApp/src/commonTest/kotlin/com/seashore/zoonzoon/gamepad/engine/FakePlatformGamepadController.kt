package com.seashore.zoonzoon.gamepad.engine

import com.seashore.zoonzoon.gamepad.model.ConnectionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Fake implementation of a gamepad controller for testing.
 * 
 * This fake records all vibration commands sent to it and allows tests to
 * control the connection state. It provides a test-friendly interface that
 * mimics PlatformGamepadController behavior.
 */
class FakePlatformGamepadController : GamepadControllerWithState {
    data class Command(val leftMotor: Float, val rightMotor: Float)
    
    val commands = mutableListOf<Command>()
    
    // Default to Connected state for testing purposes
    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Connected("Test Controller"))
    override val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()
    
    /**
     * Set the connection state for testing purposes.
     */
    fun setConnectionState(state: ConnectionState) {
        _connectionState.value = state
    }
    
    /**
     * Reset the recorded commands.
     */
    fun reset() {
        commands.clear()
    }
    
    /**
     * Get the last command sent, or null if no commands were sent.
     */
    fun getLastCommand(): Command? = commands.lastOrNull()
    
    /**
     * Get all stop commands (commands with both motors at 0).
     */
    fun getStopCommands(): List<Command> = commands.filter { it.leftMotor == 0f && it.rightMotor == 0f }
    
    override suspend fun sendVibrationCommand(leftMotor: Float, rightMotor: Float): Result<Unit> {
        commands.add(Command(leftMotor, rightMotor))
        return Result.success(Unit)
    }
    
    override suspend fun startDiscovery() {
        _connectionState.value = ConnectionState.Scanning
    }

    override suspend fun stopDiscovery() {
        if (_connectionState.value is ConnectionState.Scanning) {
            _connectionState.value = ConnectionState.Disconnected
        }
    }

    override suspend fun disconnect() {
        _connectionState.value = ConnectionState.Disconnected
    }}
