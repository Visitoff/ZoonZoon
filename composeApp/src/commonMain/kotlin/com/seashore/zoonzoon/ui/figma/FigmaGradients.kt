package com.seashore.zoonzoon.ui.figma

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/** Gradients from Figma Workspace → Themes / Buttons (`2097:13304`). */
object FigmaGradients {

    val CoralRadial = radialPrimary(
        listOf(
            Color(0xFFF1535E),
            Color(0xFFEF5284),
            Color(0xFFED51A9),
            Color(0xFFE84FF4)
        )
    )

    val NavActiveCoral = Brush.radialGradient(
        colorStops = arrayOf(
            0.00f to Color(0xFFF1535E),
            0.25f to Color(0xFFEF5284),
            0.50f to Color(0xFFED51A9),
            1.00f to Color(0xFFE84FF4)
        ),
        center = Offset(0.5f, 1f),
        radius = 1.167f
    )

    fun intensityBarBrush(barHeightPx: Float): Brush = Brush.radialGradient(
        colorStops = arrayOf(
            0.00f to Color(0xFFF1535E),
            0.25f to Color(0xFFEF5284),
            0.50f to Color(0xFFED51A9),
            1.00f to Color(0xFFE84FF4)
        ),
        center = Offset(0.5f, 1f),
        radius = (barHeightPx / 44f).coerceAtLeast(0.5f)
    )

    val SignActiveGlow = radialPrimary(
        listOf(
            Color(0xFFF1535E),
            Color(0xFFEF5284).copy(alpha = 0.7725f),
            Color(0xFFED51A9).copy(alpha = 0.545f),
            Color(0xFFE84FF4).copy(alpha = 0.09f)
        )
    )

    val HeartStroke = Brush.linearGradient(
        0.00f to Color(0xFFF4555C),
        0.50f to Color(0x00E07ADF),
        1.00f to Color(0xFFD448DF)
    )

    private fun radialPrimary(stops: List<Color>): Brush = Brush.radialGradient(
        colorStops = arrayOf(
            0.00f to stops[0],
            0.25f to stops[1],
            0.50f to stops[2],
            1.00f to stops[3]
        ),
        center = Offset(0.5f, 1f),
        radius = 1.4f
    )
}

val FigmaCoralStart = Color(0xFFF1535E)
