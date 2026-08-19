package com.seashore.zoonzoon.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.seashore.zoonzoon.generated.resources.Res
import com.seashore.zoonzoon.generated.resources.fig_eq_wave_hi
import com.seashore.zoonzoon.generated.resources.fig_eq_wave_lo
import com.seashore.zoonzoon.generated.resources.home_ic_power
import com.seashore.zoonzoon.ui.figma.FigmaCircleButton
import com.seashore.zoonzoon.ui.figma.FigmaFill
import com.seashore.zoonzoon.ui.figma.FigmaSignButton
import com.seashore.zoonzoon.ui.figma.FigmaTokens
import com.seashore.zoonzoon.ui.figma.figmaGilroy
import org.jetbrains.compose.resources.painterResource

internal val HomeFrameWidth = 375.dp
internal val HomeFrameHeight = 812.dp

private val PlayerWidth = 345.dp
private val PlayerHeight = 445.dp
private val EqClipRadius = 38.dp

private val BottomOverlayShape = RoundedCornerShape(
    topStart = 16.dp,
    topEnd = 16.dp,
    bottomStart = 56.dp,
    bottomEnd = 56.dp
)

/** Figma `-Player` @ (15, 135): 345×445. */
@Composable
fun Player(
    patternName: String,
    patternDescription: String,
    vibrationEnabled: Boolean,
    onToggleVibration: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.requiredSize(PlayerWidth, PlayerHeight)
            .clip(RoundedCornerShape(FigmaTokens.Radius.r56))
            .background(FigmaTokens.Color.surface15)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(EqClipRadius))
                .background(Color.Black)
        ) {
            // Wave 2 — Figma 2097:12037: left -59, 510×433, center Y = 50% - 388
            Image(
                painter = painterResource(Res.drawable.fig_eq_wave_hi),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .requiredSize(510.dp, 433.dp)
                    .offset(x = (-59).dp, y = (-382).dp)
            )
            // Wave 1 — Figma 2097:12036: left -59.04, 473.907×433.038, center Y = 50% + 6
            Image(
                painter = painterResource(Res.drawable.fig_eq_wave_lo),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .requiredSize(473.907.dp, 433.038.dp)
                    .offset(x = (-59.04).dp, y = 12.02.dp)
            )
            // Shadow — Figma 2097:12075: 402×118, bottom, rgba(0,0,0,0.75)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(x = 0.5.dp)
                    .requiredSize(402.dp, 118.dp)
                    .clip(BottomOverlayShape)
                    .background(Color.Black.copy(alpha = 0.75f))
            )
            // Content — Figma 2097:12208 @ left 20, top 40 in 345×120 bottom area
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .requiredSize(PlayerWidth, 120.dp)
            ) {
                Equalizer(
                    patternName = patternName,
                    patternDescription = patternDescription,
                    vibrationEnabled = vibrationEnabled,
                    onToggleVibration = onToggleVibration,
                    modifier = Modifier.offset(x = FigmaTokens.Spacing.s20, y = 40.dp)
                )
            }
        }
    }
}

@Composable
private fun Equalizer(
    patternName: String,
    patternDescription: String,
    vibrationEnabled: Boolean,
    onToggleVibration: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.size(304.dp, 60.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FigmaSignButton(
                emoji = "💗",
                selected = true,
                onClick = {}
            )
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = patternName,
                    color = FigmaTokens.Color.white,
                    style = figmaGilroy(size = 16),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = patternDescription,
                    color = FigmaTokens.Color.white.copy(alpha = 0.50f),
                    style = figmaGilroy(size = 14),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        FigmaCircleButton(
            onClick = onToggleVibration,
            size = 60.dp,
            fill = if (vibrationEnabled) FigmaFill.Light else FigmaFill.Dark
        ) {
            Image(
                painter = painterResource(Res.drawable.home_ic_power),
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
