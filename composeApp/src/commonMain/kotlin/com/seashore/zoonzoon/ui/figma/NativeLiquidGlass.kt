package com.seashore.zoonzoon.ui.figma

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import dev.chrisbanes.haze.HazeState

val LocalHazeState = staticCompositionLocalOf<HazeState?> { null }

expect fun supportsNativeLiquidGlass(): Boolean

@Composable
expect fun NativeLiquidGlass(
    modifier: Modifier,
    cornerRadius: Dp,
    tint: Color,
    interactive: Boolean
)
