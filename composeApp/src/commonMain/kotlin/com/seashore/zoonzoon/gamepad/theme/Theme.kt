package com.seashore.zoonzoon.gamepad.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.seashore.zoonzoon.settings.AppThemeMode

@Composable
fun GamepadVibratorTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    val colorScheme = if (darkTheme) {
        GamepadVibratorDarkColorScheme
    } else {
        GamepadVibratorLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ZoonZoonTypography,
        shapes = ZoonZoonShapes,
        content = content
    )
}
