package com.seashore.zoonzoon

import com.seashore.zoonzoon.gamepad.platform.PlatformGamepadController

/**
 * iOS actual implementation of createPlatformController.
 *
 * **Validates: Requirements 1.1, 1.9, 9.2, 9.3**
 */
actual fun createPlatformController(): PlatformGamepadController {
    return PlatformGamepadController()
}
