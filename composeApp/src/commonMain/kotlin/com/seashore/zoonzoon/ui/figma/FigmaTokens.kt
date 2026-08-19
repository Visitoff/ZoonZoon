package com.seashore.zoonzoon.ui.figma

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Values from `design/tokens.json` (inferred-from-frequency). */
object FigmaTokens {
    object Color {
        val black = androidx.compose.ui.graphics.Color(0xFF000000)
        val surface0E = androidx.compose.ui.graphics.Color(0xFF0E0E0E)
        val surface10 = androidx.compose.ui.graphics.Color(0xFF101010)
        val surfaceDark = androidx.compose.ui.graphics.Color(0xFF05080A)
        val surface15 = androidx.compose.ui.graphics.Color(0xFF151515)
        val lock = androidx.compose.ui.graphics.Color(0xFF1A1A1B)
        val surface1D = androidx.compose.ui.graphics.Color(0xFF1D1D1D)
        val surface1E = androidx.compose.ui.graphics.Color(0xFF1E1E1E)
        val fill333 = androidx.compose.ui.graphics.Color(0xFF333035)
        val muted = androidx.compose.ui.graphics.Color(0xFF5C5C5C)
        val iconIdle = androidx.compose.ui.graphics.Color(0xFF6C6C6C)
        val iconLock = androidx.compose.ui.graphics.Color(0xFF8E8E8E)
        val gray = androidx.compose.ui.graphics.Color(0xFFD9D9D9)
        val white = androidx.compose.ui.graphics.Color(0xFFFFFFFF)
    }

    object Spacing {
        val s0 = 0.dp
        val s1 = 1.dp
        val s5 = 5.dp
        val s6 = 6.dp
        val s10 = 10.dp
        val s15 = 15.dp
        val s20 = 20.dp
        val s26 = 26.dp
    }

    object Radius {
        val r56 = 56.dp
        val r38 = 38.dp
        val r53 = 53.dp
        val r57 = 57.dp
        val r100 = 100.dp
    }
}

/** Leaf typography from Screen 23 IR (`tokens.json.typography` is empty in v0.4). */
fun figmaGilroy(
    size: Int,
    weight: FontWeight = FontWeight.SemiBold,
    lineHeight: Int = 24,
    letterSpacing: Float = -0.6f
): TextStyle = TextStyle(
    fontSize = size.sp,
    fontWeight = weight,
    lineHeight = lineHeight.sp,
    letterSpacing = letterSpacing.sp
)

val FigmaBlack = FigmaTokens.Color.black
val FigmaSurface = FigmaTokens.Color.surface15
val FigmaFillDark = FigmaTokens.Color.fill333.copy(alpha = 0.20f)
val FigmaFillLight = FigmaTokens.Color.white.copy(alpha = 0.20f)
val FigmaFillLock = FigmaTokens.Color.lock.copy(alpha = 0.50f)
val FigmaIconIdle = FigmaTokens.Color.iconIdle
