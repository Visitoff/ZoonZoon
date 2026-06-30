package com.seashore.zoonzoon

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.tooling.preview.Preview
import com.seashore.zoonzoon.gamepad.engine.VibrationEngine
import com.seashore.zoonzoon.gamepad.platform.PlatformGamepadController
import com.seashore.zoonzoon.gamepad.platform.createPhoneVibrator
import com.seashore.zoonzoon.gamepad.theme.GamepadVibratorTheme
import com.seashore.zoonzoon.gamepad.viewmodel.GamepadViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.seashore.zoonzoon.settings.createAppSettings
import com.seashore.zoonzoon.ui.shell.ZoonZoonAppShell
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
@Preview
fun App(controller: PlatformGamepadController? = null) {
    val scope = rememberCoroutineScope()

    val resolvedController = remember(controller) {
        controller ?: createPlatformController()
    }

    val appSettings = remember { createAppSettings() }
    val phoneVibrator = remember { createPhoneVibrator() }
    val vibrationTarget = remember {
        MutableStateFlow(appSettings.getVibrationTarget())
    }

    val engine = remember(resolvedController, vibrationTarget) {
        VibrationEngine(
            controller = resolvedController,
            scope = scope,
            phoneVibrator = phoneVibrator,
            vibrationTarget = vibrationTarget
        )
    }

    val viewModel = remember(engine, vibrationTarget) {
        GamepadViewModel(
            vibrationEngine = engine,
            scope = scope,
            appSettings = appSettings,
            vibrationTargetFlow = vibrationTarget
        )
    }

    val themeMode by viewModel.themeMode.collectAsState()

    GamepadVibratorTheme(themeMode = themeMode) {
        LaunchedEffect(viewModel) {
            viewModel.onAppStarted()
        }
        ZoonZoonAppShell(viewModel = viewModel)
    }
}

expect fun createPlatformController(): PlatformGamepadController
