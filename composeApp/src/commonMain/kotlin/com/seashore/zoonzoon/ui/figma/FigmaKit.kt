package com.seashore.zoonzoon.ui.figma

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.seashore.zoonzoon.ui.fig.figCoralRadial
import com.seashore.zoonzoon.ui.glass.GlassTintLight
import com.seashore.zoonzoon.ui.glass.isLiquidGlassEnabled
import com.seashore.zoonzoon.ui.glass.liquidGlass
import dev.chrisbanes.haze.blur.blurEffect
import dev.chrisbanes.haze.blur.materials.CupertinoMaterials
import dev.chrisbanes.haze.hazeEffect

enum class FigmaFill { Dark, Light, Lock, Coral, Power }

enum class FigmaStroke { None, Heart }

@Composable
fun FigmaCircleButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 60.dp,
    fill: FigmaFill = FigmaFill.Dark,
    stroke: FigmaStroke = FigmaStroke.None,
    enabled: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    FigmaFillButton(
        onClick = onClick,
        modifier = modifier.size(size),
        fill = fill,
        shape = CircleShape,
        stroke = stroke,
        enabled = enabled,
        cornerRadius = size / 2,
        content = content
    )
}

@Composable
fun FigmaPillButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    fill: FigmaFill = FigmaFill.Dark,
    enabled: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    FigmaFillButton(
        onClick = onClick,
        modifier = modifier,
        fill = fill,
        shape = RoundedCornerShape(FigmaTokens.Radius.pill),
        stroke = FigmaStroke.None,
        enabled = enabled,
        cornerRadius = 99.dp,
        content = content
    )
}

@Composable
private fun FigmaFillButton(
    onClick: () -> Unit,
    modifier: Modifier,
    fill: FigmaFill,
    shape: Shape,
    stroke: FigmaStroke,
    enabled: Boolean,
    cornerRadius: Dp,
    content: @Composable BoxScope.() -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val nativeGlass = supportsNativeLiquidGlass()
    val liquidGlass = isLiquidGlassEnabled()
    val hazeState = LocalHazeState.current
    val blurStyle = if (
        hazeState != null &&
        !nativeGlass &&
        !liquidGlass &&
        fill != FigmaFill.Coral &&
        fill != FigmaFill.Power
    ) {
        CupertinoMaterials.regular()
    } else {
        null
    }
    val tint = when (fill) {
        FigmaFill.Dark -> FigmaFillDark
        FigmaFill.Light -> FigmaFillLight
        FigmaFill.Lock -> FigmaFillLock
        FigmaFill.Coral -> Color.White.copy(alpha = 0.08f)
        FigmaFill.Power -> GlassTintLight
    }
    val fillMod = when (fill) {
        FigmaFill.Dark -> Modifier.background(FigmaFillDark, shape)
        FigmaFill.Light -> Modifier.background(FigmaFillLight, shape)
        FigmaFill.Lock -> Modifier.background(FigmaFillLock, shape)
        FigmaFill.Coral -> Modifier.background(FigmaGradients.NavActiveCoral, shape)
        FigmaFill.Power -> Modifier.background(FigmaTokens.Color.powerButton, shape)
    }

    Box(
        modifier = modifier
            .then(if (liquidGlass) Modifier else Modifier.clip(shape))
            .then(
                if (stroke == FigmaStroke.Heart) {
                    Modifier.border(1.dp, FigmaGradients.HeartStroke, shape)
                } else {
                    Modifier
                }
            )
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        when {
            liquidGlass -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .liquidGlass(shape, tint)
                        .then(
                            if (fill == FigmaFill.Coral) Modifier.figCoralRadial(alpha = 0.45f)
                            else Modifier
                        )
                )
            }
            nativeGlass -> {
                NativeLiquidGlass(
                    modifier = Modifier.fillMaxSize(),
                    cornerRadius = cornerRadius,
                    tint = tint,
                    interactive = enabled
                )
            }
            hazeState != null && blurStyle != null -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .hazeEffect(state = hazeState) {
                            blurEffect {
                                style = blurStyle
                                noiseFactor = 0.06f
                            }
                        }
                        .then(fillMod)
                )
            }
            else -> {
                Box(Modifier.fillMaxSize().then(fillMod))
            }
        }
        content()
    }
}
