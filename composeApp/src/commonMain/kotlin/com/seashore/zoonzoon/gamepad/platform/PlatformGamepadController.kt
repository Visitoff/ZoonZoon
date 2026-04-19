package com.seashore.zoonzoon.gamepad.platform

import com.seashore.zoonzoon.gamepad.engine.GamepadControllerWithState
import com.seashore.zoonzoon.gamepad.model.ConnectionState
import kotlinx.coroutines.flow.StateFlow

/**
 * Platform-specific gamepad controller.
 *
 * Expect class — each platform provides an actual implementation:
 * - Android: Bluetooth/USB HID via Android APIs
 * - iOS: MFi controllers via GameController Framework
 *
 * **Validates: Requirements 6.1, 6.2, 6.3, 9.3**
 */
expect class PlatformGamepadController : GamepadControllerWithState {

    override val connectionState: StateFlow<ConnectionState>

    override suspend fun startDiscovery()

    override suspend fun stopDiscovery()

    override suspend fun sendVibrationCommand(leftMotor: Float, rightMotor: Float): Result<Unit>

    override suspend fun disconnect()
}
