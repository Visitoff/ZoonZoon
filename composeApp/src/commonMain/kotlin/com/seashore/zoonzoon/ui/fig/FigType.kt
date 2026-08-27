package com.seashore.zoonzoon.ui.fig

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp
import com.seashore.zoonzoon.generated.resources.Gilroy_Regular
import com.seashore.zoonzoon.generated.resources.Gilroy_Semibold
import com.seashore.zoonzoon.generated.resources.Res
import org.jetbrains.compose.resources.Font

val FigGilroy: FontFamily
    @Composable
    get() = FontFamily(
        Font(Res.font.Gilroy_Regular, FontWeight.Normal),
        Font(Res.font.Gilroy_Semibold, FontWeight.SemiBold)
    )

/**
 * Gilroy's cap height as a fraction of the font size, measured off the Figma render.
 *
 * Every text node in the design uses `text-box-trim: trim-both` with `text-box-edge:
 * cap_alphabetic`, so the box Figma reports spans the cap height only — the 24 in
 * `font/line-height/body/medium` never shows up as layout space around a single line. Sizing the
 * line box to the cap height and centring the glyph in it reproduces those bounds, which is what
 * the surrounding gaps and offsets are measured against.
 */
private const val GilroyCapRatio = 0.72f

@Composable
fun figText(
    size: Int,
    weight: FontWeight = FontWeight.SemiBold,
    letterSpacing: Float = -0.6f
): TextStyle = TextStyle(
    fontFamily = FigGilroy,
    fontSize = size.sp,
    fontWeight = weight,
    lineHeight = (size * GilroyCapRatio).sp,
    letterSpacing = letterSpacing.sp,
    lineHeightStyle = LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Center,
        trim = LineHeightStyle.Trim.None
    )
)

/** The pattern emoji keeps Figma's untrimmed `leading-[1.2]` box. */
@Composable
fun figEmoji(size: Int): TextStyle = TextStyle(
    fontFamily = FigGilroy,
    fontSize = size.sp,
    fontWeight = FontWeight.Normal,
    lineHeight = (size * 1.2f).sp
)
