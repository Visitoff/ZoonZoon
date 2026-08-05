package com.seashore.zoonzoon.gamepad.engine

/**
 * Common interface for sending vibration commands to a gamepad controller.
 *
 * [VibrationEngine] depends on this interface rather than the platform-specific
 * [com.seashore.zoonzoon.gamepad.platform.PlatformGamepadController] directly,
 * enabling testability and clean separation of concerns.
 *
 * **Validates: Requirement 6.5** – the VibrationEngine uses the same interface
 * regardless of controller-specific protocol differences.
 */
interface GamepadController {
    /**
     * Send a vibration command to the connected controller.
     *
     * @param leftMotor  Left motor intensity in range [0.0, 1.0].
     * @param rightMotor Right motor intensity in range [0.0, 1.0].
     * @return [Result.success] on success, [Result.failure] with an error on failure.
     */
    suspend fun sendVibrationCommand(leftMotor: Float, rightMotor: Float): Result<Unit>

    /**
     * Haptic sharpness for platforms that support it (iOS Core Haptics). Range [0.0, 1.0].
     * Default no-op for Android / fakes.
     */
    fun setHapticSharpness(sharpness: Float) = Unit
}
