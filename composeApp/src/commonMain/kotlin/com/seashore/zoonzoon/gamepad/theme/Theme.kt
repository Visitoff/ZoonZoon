package com.seashore.zoonzoon.gamepad.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

/**
 * GamepadVibratorTheme provides a pink pastel Material3 theme for the Gamepad Vibration Controller app.
 * 
 * This theme implements the pink pastel color scheme as specified in Requirement 7.3.
 * It supports both light and dark theme variants that automatically adapt to system preferences.
 * 
 * @param darkTheme Whether to use the dark theme variant. Defaults to system preference.
 * @param content The composable content to be themed.
 */
@Composable
fun GamepadVibratorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        GamepadVibratorDarkColorScheme
    } else {
        GamepadVibratorLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MaterialTheme.typography, // Use default Material3 typography
        shapes = MaterialTheme.shapes, // Use default Material3 shapes
        content = content
    )
}