package com.seashore.zoonzoon.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.seashore.zoonzoon.generated.resources.Res
import com.seashore.zoonzoon.generated.resources.home_eq_hi
import com.seashore.zoonzoon.generated.resources.home_eq_lo
import com.seashore.zoonzoon.generated.resources.ic_power
import com.seashore.zoonzoon.ui.fig.FigAsset
import com.seashore.zoonzoon.ui.fig.FigCircleButton
import com.seashore.zoonzoon.ui.fig.FigColor
import com.seashore.zoonzoon.ui.fig.figEmoji
import com.seashore.zoonzoon.ui.fig.figNodeSize
import com.seashore.zoonzoon.ui.fig.figText
import com.seashore.zoonzoon.ui.glass.GlassTintLight
import com.seashore.zoonzoon.ui.glass.ProvideGlassBackdrop
import com.seashore.zoonzoon.ui.glass.glassSource
import com.seashore.zoonzoon.ui.glass.isLiquidGlassEnabled

/** Figma `-Player` (4028:2872): 345×445 at (15, 135). */
val PlayerWidth = 345.dp
val PlayerHeight = 445.dp
val PlayerX = 15.dp
val PlayerY = 135.dp

private val EqualizerRadius = 38.dp
private val PlayerRadius = 56.dp

/** `-content` is 304 wide and holds the 60px sign, a 10px gap and the 60px power button. */
private val CaptionTextMaxWidth = 304.dp - 60.dp - 10.dp - 60.dp

/** `emoji-wrapper` is 24 high with 4 of top padding, which nudges the glyph down by half of it. */
private val EmojiTopPadding = 2.dp

/** `Shadow` (2097:12075) is wider than the card on purpose and is cut by the equalizer's clip. */
private val ShadowShape = RoundedCornerShape(
    topStart = 16.dp,
    topEnd = 16.dp,
    bottomStart = 56.dp,
    bottomEnd = 56.dp
)

@Composable
fun Player(
    patternName: String,
    patternDescription: String,
    patternEmoji: String,
    onToggleVibration: () -> Unit,
    modifier: Modifier = Modifier
) {
    ProvideGlassBackdrop {
        Box(
            modifier = modifier
                .size(PlayerWidth, PlayerHeight)
                .clip(RoundedCornerShape(PlayerRadius))
                .background(FigColor.surface)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(EqualizerRadius))
                    .background(FigColor.black)
                    .glassSource()
            ) {
                FigAsset(
                    resource = Res.drawable.home_eq_lo,
                    width = 473.907.dp,
                    height = 433.038.dp,
                    modifier = Modifier.offset(x = (-59.04).dp, y = 11.981.dp)
                )
                FigAsset(
                    resource = Res.drawable.home_eq_hi,
                    width = 510.dp,
                    height = 433.dp,
                    modifier = Modifier.offset(x = (-59).dp, y = (-382).dp)
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .size(PlayerWidth, 120.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .offset(x = (-28).dp)
                            .figNodeSize(402.dp, 118.dp)
                            .clip(ShadowShape)
                            .background(FigColor.playerShadow)
                    )
                }
            }
            Caption(
                patternName = patternName,
                patternDescription = patternDescription,
                patternEmoji = patternEmoji,
                onToggleVibration = onToggleVibration,
                modifier = Modifier.align(Alignment.BottomStart)
            )
        }
    }
}

/** Figma `-bottom` (2097:12229): a 345×120 strip pinned to the bottom of the equalizer. */
@Composable
private fun Caption(
    patternName: String,
    patternDescription: String,
    patternEmoji: String,
    onToggleVibration: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.size(PlayerWidth, 120.dp)) {
        Row(
            modifier = Modifier
                .offset(x = 20.dp, y = 40.dp)
                .width(304.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FigCircleButton(
                    onClick = {},
                    diameter = 60.dp,
                    borderColor = FigColor.signBorder
                ) {
                    Text(
                        text = patternEmoji,
                        style = figEmoji(size = 24),
                        modifier = Modifier.offset(y = EmojiTopPadding)
                    )
                }
                Column(
                    modifier = Modifier
                        .height(60.dp)
                        .widthIn(max = CaptionTextMaxWidth),
                    verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically)
                ) {
                    Text(
                        text = patternName,
                        color = FigColor.white,
                        style = figText(size = 16),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = patternDescription,
                        color = FigColor.white.copy(alpha = 0.50f),
                        style = figText(size = 14),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            FigCircleButton(
                onClick = onToggleVibration,
                diameter = 60.dp,
                fill = if (isLiquidGlassEnabled()) GlassTintLight else FigColor.buttonSolid
            ) {
                FigAsset(
                    resource = Res.drawable.ic_power,
                    width = 12.dp,
                    height = 12.75.dp
                )
            }
        }
    }
}
