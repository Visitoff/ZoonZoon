package com.seashore.zoonzoon.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seashore.zoonzoon.gamepad.theme.LocalGlassTokens
import com.seashore.zoonzoon.gamepad.theme.ZoonZoonAccentEnd
import com.seashore.zoonzoon.gamepad.theme.ZoonZoonAccentStart
import com.seashore.zoonzoon.gamepad.theme.glassSurface

/**
 * Liquid-glass vibrate orb. OFF = frosted glass disc with an accent ring;
 * ON = accent-gradient orb with a soft outer glow and a glass highlight sheen.
 */
@Composable
fun NeomorphicVibrateButton(
    enabled: Boolean,
    onClick: () -> Unit,
    offLabel: String = "VIBRATE",
    onLabel: String = "ON",
    modifier: Modifier = Modifier
) {
    val tokens = LocalGlassTokens.current
    val interactionSource = remember { MutableInteractionSource() }
    val size = 208.dp

    val glow by animateFloatAsState(
        targetValue = if (enabled) 1f else 0f,
        animationSpec = tween(durationMillis = 280),
        label = "glow"
    )
    val textColor by animateColorAsState(
        targetValue = if (enabled) Color.White else ZoonZoonAccentStart,
        animationSpec = tween(durationMillis = 280),
        label = "vibrateTextColor"
    )

    val baseShadow = modifier
        .size(size)
        .shadow(
            elevation = (18 + 26 * glow).dp,
            shape = CircleShape,
            clip = false,
            ambientColor = ZoonZoonAccentStart.copy(alpha = 0.55f * glow + 0.05f),
            spotColor = ZoonZoonAccentEnd.copy(alpha = 0.6f * glow + 0.05f)
        )

    val bodyModifier = if (enabled) {
        baseShadow
            .clip(CircleShape)
            .background(Brush.verticalGradient(listOf(ZoonZoonAccentStart, ZoonZoonAccentEnd)))
    } else {
        baseShadow.glassSurface(shape = CircleShape, tokens = tokens, strong = true, elevation = 0.dp)
    }

    Box(
        modifier = bodyModifier
            .border(
                BorderStroke(
                    width = if (enabled) 1.dp else 2.dp,
                    brush = Brush.linearGradient(
                        if (enabled) {
                            listOf(Color.White.copy(alpha = 0.6f), Color.White.copy(alpha = 0.05f))
                        } else {
                            listOf(ZoonZoonAccentStart, ZoonZoonAccentEnd)
                        }
                    )
                ),
                shape = CircleShape
            )
            .drawBehind {
                // Top glass sheen highlight
                val highlightRadius = size.toPx() * 0.42f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.White.copy(alpha = 0.45f), Color.Transparent),
                        center = Offset(this.size.width * 0.34f, this.size.height * 0.26f),
                        radius = highlightRadius
                    ),
                    radius = highlightRadius,
                    center = Offset(this.size.width * 0.34f, this.size.height * 0.26f)
                )
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (enabled) onLabel else offLabel,
            color = textColor,
            fontSize = if (enabled) 34.sp else 18.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = if (enabled) 1.sp else 2.sp,
            textAlign = TextAlign.Center
        )
    }
}
