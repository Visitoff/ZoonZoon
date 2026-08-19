package com.seashore.zoonzoon.ui.figma

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

actual fun supportsNativeLiquidGlass(): Boolean = true

/**
 * Android approximation of Figma liquid glass (blur + tint + specular highlight).
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
            .blur(12.dp)
            .background(tint.copy(alpha = tint.alpha.coerceAtLeast(0.18f)), shape)
            .drawBehind {
                val r = size.minDimension * 0.55f
                val center = Offset(size.width * 0.32f, size.height * 0.22f)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.White.copy(alpha = 0.42f), Color.Transparent),
                        center = center,
                        radius = r
                    ),
                    radius = r,
                    center = center
                )
            }
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.White.copy(alpha = 0.14f),
                        1f to Color.Transparent
                    )
                )
        )
    }
}
