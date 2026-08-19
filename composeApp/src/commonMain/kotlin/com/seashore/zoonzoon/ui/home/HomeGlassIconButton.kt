package com.seashore.zoonzoon.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.seashore.zoonzoon.gamepad.theme.LocalGlassTokens
import com.seashore.zoonzoon.gamepad.theme.glassSurface

/** Circular liquid-glass control from the Figma Home kit. */
@Composable
fun HomeGlassIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 45.dp,
    strong: Boolean = false,
    enabled: Boolean = true,
    borderColor: Color? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val tokens = LocalGlassTokens.current
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .size(size)
            .glassSurface(
                shape = CircleShape,
                tokens = tokens,
                strong = strong,
                elevation = 8.dp
            )
            .then(
                if (borderColor != null) {
                    Modifier.border(BorderStroke(1.dp, borderColor), CircleShape)
                } else {
                    Modifier
                }
            )
            .drawBehind {
                val highlightRadius = this.size.minDimension * 0.55f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.White.copy(alpha = 0.38f), Color.Transparent),
                        center = Offset(this.size.width * 0.32f, this.size.height * 0.22f),
                        radius = highlightRadius
                    ),
                    radius = highlightRadius,
                    center = Offset(this.size.width * 0.32f, this.size.height * 0.22f)
                )
            }
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center,
        content = content
    )
}
