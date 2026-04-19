package com.seashore.zoonzoon

import android.app.Application
import com.seashore.zoonzoon.gamepad.platform.PlatformGamepadController

// Application instance held for context access
private lateinit var appContext: Application

/**
 * Android Application class to hold context.
 */
class GamepadApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        appContext = this
    }
}

/**
 * Android actual implementation of createPlatformController.
 * Creates an AndroidGamepadController with the application context.
 *
 * **Validates: Requirements 1.1, 1.8, 9.2, 9.3**
 */
actual fun createPlatformController(): PlatformGamepadController {
    return PlatformGamepadController(appContext)
}
