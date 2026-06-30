package com.seashore.zoonzoon.gamepad.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
    val glassTokens = if (darkTheme) DarkGlassTokens else LightGlassTokens

    CompositionLocalProvider(
        LocalGlassTokens provides glassTokens,
        LocalAppDarkTheme provides darkTheme
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = ZoonZoonTypography,
            shapes = ZoonZoonShapes,
            content = content
        )
    }
}
