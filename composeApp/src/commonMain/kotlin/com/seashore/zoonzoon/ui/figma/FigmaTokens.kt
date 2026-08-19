package com.seashore.zoonzoon.ui.figma

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seashore.zoonzoon.generated.resources.Gilroy_Regular
import com.seashore.zoonzoon.generated.resources.Gilroy_Semibold
import com.seashore.zoonzoon.generated.resources.Res
import org.jetbrains.compose.resources.Font

object FigmaTokens {
    object Color {
        val black = Color(0xFF000000)
        val surface15 = Color(0xFF151515)
        val surfaceDark = Color(0xFF05080A)
        val powerButton = Color(0xFF19181A)
        val barInactive = Color(0xFF212121)
        val fill333 = Color(0xFF333035)
        val heartBorder = Color(0xFFF4555C)
        val iconIdle = Color(0xFF6C6C6C)
        val white = Color(0xFFFFFFFF)
    }

    object Spacing {
        val s5 = 5.dp
        val s10 = 10.dp
        val s15 = 15.dp
        val s20 = 20.dp
    }

    object Radius {
        val pill = 999.dp
        val r38 = 38.dp
        val r56 = 56.dp
        val r100 = 100.dp
    }
}

val FigmaBlack = FigmaTokens.Color.black
val FigmaFillDark = FigmaTokens.Color.fill333.copy(alpha = 0.20f)
val FigmaFillLight = FigmaTokens.Color.white.copy(alpha = 0.20f)
val FigmaFillLock = Color(0xFF1A1A1B).copy(alpha = 0.50f)
val FigmaIconIdle = FigmaTokens.Color.iconIdle

val GilroyFontFamily: FontFamily
    @Composable
    get() = FontFamily(
        Font(Res.font.Gilroy_Regular, FontWeight.Normal),
        Font(Res.font.Gilroy_Semibold, FontWeight.SemiBold)
    )

@Composable
fun figmaGilroy(
    size: Int,
    weight: FontWeight = FontWeight.SemiBold,
    lineHeight: Int = 24,
    letterSpacing: Float = -0.6f
): TextStyle = TextStyle(
    fontFamily = GilroyFontFamily,
    fontSize = size.sp,
    fontWeight = weight,
    lineHeight = lineHeight.sp,
    letterSpacing = letterSpacing.sp
)
