package com.seashore.zoonzoon

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.tooling.preview.Preview
import com.seashore.zoonzoon.gamepad.engine.VibrationEngine
import com.seashore.zoonzoon.gamepad.platform.PlatformGamepadController
import com.seashore.zoonzoon.gamepad.theme.GamepadVibratorTheme
import com.seashore.zoonzoon.gamepad.ui.GamepadVibratorScreen
import com.seashore.zoonzoon.gamepad.viewmodel.GamepadViewModel

/**
 * App entry point composable.
 *
 * Wires together PlatformGamepadController → VibrationEngine → GamepadViewModel → UI.
 *
 * **Validates: Requirements 1.1, 7.1, 7.2, 9.1, 9.2, 9.3**
 */
@Composable
@Preview
fun App(controller: PlatformGamepadController? = null) {
    val scope = rememberCoroutineScope()

    val resolvedController = remember(controller) {
        controller ?: createPlatformController()
    }

    val engine = remember(resolvedController) {
        VibrationEngine(resolvedController, scope)
    }

    val viewModel = remember(engine) {
        GamepadViewModel(engine, scope)
    }

    GamepadVibratorTheme {
        GamepadVibratorScreen(viewModel = viewModel)
    }
}

/**
 * Expect function to create a platform-specific controller.
 * Each platform provides its own actual implementation.
 */
expect fun createPlatformController(): PlatformGamepadController
