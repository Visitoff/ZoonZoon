package com.seashore.zoonzoon.ui.glass

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy

private val LocalLayerBackdrop = staticCompositionLocalOf<LayerBackdrop?> { null }

@Composable
actual fun ProvideGlassBackdrop(content: @Composable () -> Unit) {
    val backdrop = rememberLayerBackdrop()
    CompositionLocalProvider(LocalLayerBackdrop provides backdrop, content = content)
}

@Composable
actual fun Modifier.glassSource(): Modifier {
    val backdrop = LocalLayerBackdrop.current ?: return this
    return layerBackdrop(backdrop)
}

@Composable
actual fun isLiquidGlassEnabled(): Boolean = LocalLayerBackdrop.current != null

@Composable
actual fun Modifier.liquidGlass(shape: Shape, tint: Color): Modifier {
    val backdrop = LocalLayerBackdrop.current ?: return this
    return drawBackdrop(
        backdrop = backdrop,
        shape = { shape },
        effects = {
            vibrancy()
            val radius = size.minDimension / 2f
            if (radius > 1f) {
                lens(
                    refractionHeight = radius * 0.92f,
                    refractionAmount = radius * 3.2f,
                    depthEffect = true,
                    chromaticAberration = true
                )
            }
        },
        shadow = { null },
        onDrawSurface = {
            if (tint.alpha > 0f) {
                drawRect(color = tint)
            }
        }
    )
}
