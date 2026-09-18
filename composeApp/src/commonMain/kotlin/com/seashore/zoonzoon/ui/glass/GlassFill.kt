package com.seashore.zoonzoon.ui.glass

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import com.seashore.zoonzoon.ui.figma.NativeLiquidGlass
import com.seashore.zoonzoon.ui.figma.supportsNativeLiquidGlass

/** Plugin glass (Android / iOS below 26) or native UIGlassEffect (iOS 26+). */
@Composable
fun isGlassEnabled(): Boolean = isLiquidGlassEnabled() || supportsNativeLiquidGlass()

/**
 * Backdrop fill used by buttons, chips, and the slider thumb.
 * On iOS 26+ this is a UIKit overlay so it can sample the Compose canvas underneath.
 */
@Composable
fun GlassFill(
    modifier: Modifier = Modifier,
    shape: Shape,
    tint: Color,
    cornerRadius: Dp,
    interactive: Boolean = true
) {
    when {
        isLiquidGlassEnabled() -> {
            Box(modifier = modifier.liquidGlass(shape, tint))
        }
        supportsNativeLiquidGlass() -> {
            NativeLiquidGlass(
                modifier = modifier,
                cornerRadius = cornerRadius,
                tint = tint,
                interactive = interactive
            )
        }
    }
}
