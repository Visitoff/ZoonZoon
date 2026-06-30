package com.seashore.zoonzoon.gamepad.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.blurEffect
import dev.chrisbanes.haze.blur.materials.CupertinoMaterials
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

/**
 * Theme-aware "Liquid Glass" (iOS 26) design tokens.
 * Backdrop blur is provided by [Haze](https://github.com/chrisbanes/haze) via [glassSurface].
 */
@Immutable
data class GlassTokens(
    val dark: Boolean,
    val fillTop: Color,
    val fillBottom: Color,
    val fillTopStrong: Color,
    val fillBottomStrong: Color,
    val borderTop: Color,
    val borderBottom: Color,
    val hairline: Color,
    val shadow: Color
)

val LightGlassTokens = GlassTokens(
    dark = false,
    fillTop = Color.White.copy(alpha = 0.82f),
    fillBottom = Color.White.copy(alpha = 0.55f),
    fillTopStrong = Color.White.copy(alpha = 0.94f),
    fillBottomStrong = Color.White.copy(alpha = 0.72f),
    borderTop = Color.White.copy(alpha = 0.95f),
    borderBottom = Color.White.copy(alpha = 0.35f),
    hairline = Color(0xFF1B1240).copy(alpha = 0.08f),
    shadow = Color(0xFF6A5AE0).copy(alpha = 0.20f)
)

val DarkGlassTokens = GlassTokens(
    dark = true,
    fillTop = Color.White.copy(alpha = 0.16f),
    fillBottom = Color.White.copy(alpha = 0.06f),
    fillTopStrong = Color.White.copy(alpha = 0.24f),
    fillBottomStrong = Color.White.copy(alpha = 0.10f),
    borderTop = Color.White.copy(alpha = 0.38f),
    borderBottom = Color.White.copy(alpha = 0.06f),
    hairline = Color.White.copy(alpha = 0.12f),
    shadow = Color(0xFF05010F).copy(alpha = 0.55f)
)

val LocalGlassTokens = staticCompositionLocalOf { DarkGlassTokens }

/** Shared [HazeState] for backdrop blur — provided by [GlassBackground]. */
val LocalHazeState = staticCompositionLocalOf<HazeState?> { null }

/** True when the app is rendering its dark gradient theme. */
val LocalAppDarkTheme = staticCompositionLocalOf { true }

/**
 * Frosted-glass surface: tinted shadow → clip → **Haze backdrop blur** (Cupertino material)
 * → bright top-edge highlight border.
 */
@Composable
fun Modifier.glassSurface(
    shape: Shape,
    tokens: GlassTokens,
    strong: Boolean = false,
    elevation: Dp = 12.dp,
    borderWidth: Dp = 1.dp
): Modifier {
    val hazeState = LocalHazeState.current
    val borderBrush = Brush.verticalGradient(listOf(tokens.borderTop, tokens.borderBottom))
    val blurStyle = if (strong) CupertinoMaterials.thick() else CupertinoMaterials.regular()

    var result = this
        .shadow(
            elevation = elevation,
            shape = shape,
            clip = false,
            ambientColor = tokens.shadow,
            spotColor = tokens.shadow
        )
        .clip(shape)

    result = if (hazeState != null) {
        result.hazeEffect(state = hazeState) {
            blurEffect {
                style = blurStyle
                noiseFactor = 0.06f
            }
        }
    } else {
        val fill = if (strong) {
            Brush.verticalGradient(listOf(tokens.fillTopStrong, tokens.fillBottomStrong))
        } else {
            Brush.verticalGradient(listOf(tokens.fillTop, tokens.fillBottom))
        }
        result.drawBehind { drawRect(fill) }
    }

    return result.border(BorderStroke(borderWidth, borderBrush), shape)
}

/**
 * Full-screen gradient backdrop registered as a Haze blur source.
 * Glass panels anywhere in [content] sample and blur this background in real time.
 */
@Composable
fun GlassBackground(
    dark: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val hazeState = rememberHazeState()

    CompositionLocalProvider(LocalHazeState provides hazeState) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .hazeSource(state = hazeState)
                .drawBehind { drawGlassGradientBackdrop(dark) }
        ) {
            content()
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawGlassGradientBackdrop(dark: Boolean) {
    val base = if (dark) {
        Brush.verticalGradient(
            listOf(Color(0xFF0A0A12), Color(0xFF141029), Color(0xFF0B0A16))
        )
    } else {
        Brush.verticalGradient(
            listOf(Color(0xFFEAF0FF), Color(0xFFF3EBFF), Color(0xFFFFEFF7))
        )
    }
    drawRect(base)

    val w = size.width
    val h = size.height
    val r = size.minDimension

    fun blob(color: Color, cx: Float, cy: Float, radiusScale: Float) {
        val radius = r * radiusScale
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(color, color.copy(alpha = 0f)),
                center = Offset(cx, cy),
                radius = radius
            ),
            radius = radius,
            center = Offset(cx, cy)
        )
    }

    if (dark) {
        blob(Color(0xFF4B2E83).copy(alpha = 0.60f), w * 0.12f, h * 0.02f, 0.85f)
        blob(ZoonZoonAccentStart.copy(alpha = 0.34f), w * 1.02f, h * 0.10f, 0.70f)
        blob(Color(0xFF243089).copy(alpha = 0.50f), w * 0.50f, h * 1.02f, 0.95f)
        blob(ZoonZoonAccentEnd.copy(alpha = 0.30f), w * 0.92f, h * 0.88f, 0.65f)
    } else {
        blob(Color(0xFFBFD0FF).copy(alpha = 0.70f), w * 0.10f, h * 0.00f, 0.85f)
        blob(Color(0xFFE9C7FF).copy(alpha = 0.65f), w * 1.02f, h * 0.06f, 0.70f)
        blob(Color(0xFFC8D8FF).copy(alpha = 0.60f), w * 0.50f, h * 1.02f, 0.95f)
        blob(Color(0xFFFFD2E8).copy(alpha = 0.60f), w * 0.94f, h * 0.90f, 0.65f)
    }
}
