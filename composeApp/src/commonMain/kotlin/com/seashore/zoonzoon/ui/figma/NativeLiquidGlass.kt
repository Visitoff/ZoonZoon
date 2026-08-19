package com.seashore.zoonzoon.ui.figma

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

/** True only on iOS 26+ where `UIGlassEffect` exists. */
expect fun supportsNativeLiquidGlass(): Boolean

/**
 * System Liquid Glass. On iOS 26 this is `UIVisualEffectView` + `UIGlassEffect`.
 * Other platforms must not call this; use [supportsNativeLiquidGlass] first.
 */
@Composable
expect fun NativeLiquidGlass(
    modifier: Modifier,
    cornerRadius: Dp,
    tint: Color,
    interactive: Boolean
)
