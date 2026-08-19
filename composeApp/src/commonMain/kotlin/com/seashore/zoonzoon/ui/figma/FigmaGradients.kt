package com.seashore.zoonzoon.ui.figma

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Gradients from Figma **Workspace → Themes / Buttons** (`2097:13303`, `2078:8896`).
 * Primary fills use a bottom-centered radial with 4–5 stops.
 */
object FigmaGradients {

    /** Coral radial: `#F1535E` → `#EF5284` → `#ED51A9` → `#E84FF4`. */
    val CoralRadial = radialPrimary(
        listOf(
            Color(0xFFF1535E),
            Color(0xFFEF5284),
            Color(0xFFED51A9),
            Color(0xFFE84FF4)
        )
    )

    /** Coral linear (heart border, small swatches): `#F1535E` → `#E84FF4`. */
    val CoralLinear = Brush.linearGradient(
        0f to Color(0xFFF1535E),
        1f to Color(0xFFE84FF4)
    )

    /** Sign active glow — same stops, last stop at 9% alpha (Figma `-sign` Active=Yes). */
    val SignActiveGlow = radialPrimary(
        listOf(
            Color(0xFFF1535E),
            Color(0xFFEF5284).copy(alpha = 0.7725f),
            Color(0xFFED51A9).copy(alpha = 0.545f),
            Color(0xFFE84FF4).copy(alpha = 0.09f)
        )
    )

    /** Aqua radial. */
    val AquaRadial = radialPrimary(
        listOf(
            Color(0xFF6353F1),
            Color(0xFF5E7BE3),
            Color(0xFF59A3D5),
            Color(0xFF54CCC8),
            Color(0xFF4FF4BA)
        )
    )

    /** Peach radial. */
    val PeachRadial = radialPrimary(
        listOf(
            Color(0xFFE84FF4),
            Color(0xFFEB6DCB),
            Color(0xFFEE8BA2),
            Color(0xFFF1AA78),
            Color(0xFFF4C84F)
        )
    )

    /** Tropical radial. */
    val TropicalRadial = radialPrimary(
        listOf(
            Color(0xFF31C490),
            Color(0xFF5DC077),
            Color(0xFF8ABB5E),
            Color(0xFFB6B746),
            Color(0xFFE3B22D)
        )
    )

    /** Lavender radial. */
    val LavenderRadial = radialPrimary(
        listOf(
            Color(0xFF53BCF1),
            Color(0xFF6EA1F2),
            Color(0xFF8985F3),
            Color(0xFFA56AF3),
            Color(0xFFC04FF4)
        )
    )

    /** Heart stroke: `#F4555C` → transparent → `#D448DF`. */
    val HeartStroke = Brush.linearGradient(
        0.00f to Color(0xFFF4555C),
        0.50f to Color(0x00E07ADF),
        1.00f to Color(0xFFD448DF)
    )

    fun primaryRadial(theme: FigmaTheme): Brush = when (theme) {
        FigmaTheme.Coral -> CoralRadial
        FigmaTheme.Aqua -> AquaRadial
        FigmaTheme.Peach -> PeachRadial
        FigmaTheme.Tropical -> TropicalRadial
        FigmaTheme.Lavender -> LavenderRadial
    }

    /**
     * Matches Figma radial on pill buttons: center at bottom edge, radius ≈ 1.4× height.
     * Transform from Workspace: `matrix(0, -6, 14, 0, cx, cy)` on 140×60 master.
     */
    private fun radialPrimary(stops: List<Color>): Brush {
        val pairs = stops.mapIndexed { index, color ->
            when (index) {
                0 -> 0.00f to color
                1 -> 0.25f to color
                2 -> 0.50f to color
                3 -> if (stops.size > 4) 0.75f to color else 1.00f to color
                else -> 1.00f to color
            }
        }.toTypedArray()
        return Brush.radialGradient(
            colorStops = pairs,
            center = Offset(0.5f, 1f),
            radius = 1.4f
        )
    }
}

enum class FigmaTheme { Coral, Aqua, Peach, Tropical, Lavender }

val FigmaCoralStart = Color(0xFFF1535E)
val FigmaCoralEnd = Color(0xFFE84FF4)
