package com.seashore.zoonzoon.ui.figma

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

actual fun supportsNativeLiquidGlass(): Boolean = true

/**
 * Android approximation of Figma liquid glass (tint + specular highlight, no child blur).
 * Not system UIGlassEffect — allowed per project spec on Android only.
 */
@Composable
actual fun NativeLiquidGlass(
    modifier: Modifier,
    cornerRadius: Dp,
    tint: Color,
    interactive: Boolean
) {
    val shape = RoundedCornerShape(cornerRadius)
    Box(
        modifier = modifier
            .clip(shape)
            .drawBehind {
                drawRect(tint.copy(alpha = tint.alpha.coerceAtLeast(0.22f)))
                val r = size.minDimension * 0.58f
                val highlight = Offset(size.width * 0.30f, size.height * 0.18f)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.White.copy(alpha = 0.50f), Color.Transparent),
                        center = highlight,
                        radius = r
                    ),
                    radius = r,
                    center = highlight
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.White.copy(alpha = 0.08f), Color.Transparent),
                        center = Offset(size.width * 0.72f, size.height * 0.78f),
                        radius = size.minDimension * 0.45f
                    ),
                    radius = size.minDimension * 0.45f,
                    center = Offset(size.width * 0.72f, size.height * 0.78f)
                )
            }
            .background(
                Brush.verticalGradient(
                    0f to Color.White.copy(alpha = 0.16f),
                    0.35f to Color.Transparent,
                    1f to Color.Black.copy(alpha = 0.12f)
                )
            )
    )
}
