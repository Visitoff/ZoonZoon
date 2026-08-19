package com.seashore.zoonzoon.ui.figma

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import GameControllerHaptics.LiquidGlassView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIView

actual fun supportsNativeLiquidGlass(): Boolean = LiquidGlassView.isAvailable()

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun NativeLiquidGlass(
    modifier: Modifier,
    cornerRadius: Dp,
    tint: Color,
    interactive: Boolean
) {
    val r = tint.red.toDouble()
    val g = tint.green.toDouble()
    val b = tint.blue.toDouble()
    val a = tint.alpha.toDouble()
    val radiusPts = cornerRadius.value.toDouble()
    UIKitView(
        factory = {
            LiquidGlassView.viewWithCornerRadius(
                radiusPts,
                r,
                g,
                b,
                a,
                interactive
            )
        },
        modifier = modifier,
        update = { view: UIView ->
            LiquidGlassView.updateView(
                view,
                radiusPts,
                r,
                g,
                b,
                a,
                interactive
            )
        },
        properties = UIKitInteropProperties(interactionMode = null)
    )
}
