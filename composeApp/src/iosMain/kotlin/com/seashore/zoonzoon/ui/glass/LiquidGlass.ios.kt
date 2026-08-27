package com.seashore.zoonzoon.ui.glass

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape

@Composable
actual fun ProvideGlassBackdrop(content: @Composable () -> Unit) {
    content()
}

@Composable
actual fun Modifier.glassSource(): Modifier = this

@Composable
actual fun Modifier.liquidGlass(shape: Shape, tint: Color): Modifier = this

@Composable
actual fun isLiquidGlassEnabled(): Boolean = false
