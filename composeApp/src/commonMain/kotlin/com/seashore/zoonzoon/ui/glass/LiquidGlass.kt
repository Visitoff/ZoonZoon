package com.seashore.zoonzoon.ui.glass

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape

/** Idle glass: Figma `button/fill-secondary-dark` = rgba(51,48,53,0.2). */
val GlassTintButton = Color(0x33333035)

/** Figma `button/fill-secondary-light` = rgba(255,255,255,0.2) — slider thumb, power. */
val GlassTintLight = Color.White.copy(alpha = 0.20f)

@Composable
expect fun ProvideGlassBackdrop(content: @Composable () -> Unit)

@Composable
expect fun Modifier.glassSource(): Modifier

@Composable
expect fun Modifier.liquidGlass(shape: Shape, tint: Color): Modifier

@Composable
expect fun isLiquidGlassEnabled(): Boolean
