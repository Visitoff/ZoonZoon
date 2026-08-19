package com.seashore.zoonzoon.ui.figma

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

actual fun supportsNativeLiquidGlass(): Boolean = false

@Composable
actual fun NativeLiquidGlass(
    modifier: Modifier,
    cornerRadius: Dp,
    tint: Color,
    interactive: Boolean
) = Unit
