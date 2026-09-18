package com.seashore.zoonzoon.ui.figma

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import com.seashore.zoonzoon.nativeliquidglass.ZZCreateNativeLiquidGlassView
import com.seashore.zoonzoon.nativeliquidglass.ZZNativeLiquidGlassAvailable
import com.seashore.zoonzoon.nativeliquidglass.ZZUpdateNativeLiquidGlassView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIView

@OptIn(ExperimentalForeignApi::class)
private val nativeLiquidGlassAvailable: Boolean by lazy { ZZNativeLiquidGlassAvailable() }

actual fun supportsNativeLiquidGlass(): Boolean = nativeLiquidGlassAvailable

private fun Color.channel(shift: Int): Double =
    (((toArgb() ushr shift) and 0xFF) / 255.0)

@OptIn(ExperimentalComposeUiApi::class, ExperimentalForeignApi::class)
@Composable
actual fun NativeLiquidGlass(
    modifier: Modifier,
    cornerRadius: Dp,
    tint: Color,
    interactive: Boolean
) {
    if (!nativeLiquidGlassAvailable) return
    val radius = cornerRadius.value.toDouble()
    val red = tint.channel(16)
    val green = tint.channel(8)
    val blue = tint.channel(0)
    val alpha = tint.channel(24)
    UIKitView(
        factory = {
            ZZCreateNativeLiquidGlassView(radius, red, green, blue, alpha, interactive)
        },
        modifier = modifier,
        update = { view: UIView ->
            ZZUpdateNativeLiquidGlassView(view, radius, red, green, blue, alpha, interactive)
        },
        properties = UIKitInteropProperties(placedAsOverlay = true)
    )
}
