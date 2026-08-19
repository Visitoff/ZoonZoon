package com.seashore.zoonzoon.ui.home

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.seashore.zoonzoon.ui.figma.FigmaCircleButton
import com.seashore.zoonzoon.ui.figma.FigmaEqImage
import com.seashore.zoonzoon.ui.figma.FigmaFill
import com.seashore.zoonzoon.ui.figma.FigmaPowerIcon
import com.seashore.zoonzoon.ui.figma.FigmaSignButton
import com.seashore.zoonzoon.ui.figma.FigmaTokens
import com.seashore.zoonzoon.ui.figma.figmaGilroy

val HomeFrameWidth = 375.dp
val HomeFrameHeight = 812.dp

private val PlayerWidth = 345.dp
private val PlayerHeight = 445.dp
private val EqClipRadius = 38.dp

private val BottomOverlayShape = RoundedCornerShape(
    topStart = 16.dp,
    topEnd = 16.dp,
    bottomStart = 56.dp,
    bottomEnd = 56.dp
)

/** Figma `-Player` (4028:2872): 345×445 @ y=135. */
@Composable
fun Player(
    patternName: String,
    patternDescription: String,
    patternEmoji: String,
    vibrationEnabled: Boolean,
    onToggleVibration: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .requiredSize(PlayerWidth, PlayerHeight)
            .clip(RoundedCornerShape(FigmaTokens.Radius.r56))
            .background(FigmaTokens.Color.surface15)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(EqClipRadius))
                .background(Color.Black)
        ) {
            FigmaEqImage(
                modifier = Modifier
                    .fillMaxSize()
                    .offset(x = (-20).dp, y = (-40).dp)
                    .requiredSize(400.dp, 360.dp)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(x = 0.5.dp)
                    .requiredSize(402.dp, 120.dp)
                    .clip(BottomOverlayShape)
                    .background(Color.Black.copy(alpha = 0.75f))
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .requiredSize(PlayerWidth, 120.dp)
            ) {
                Equalizer(
                    patternName = patternName,
                    patternDescription = patternDescription,
                    patternEmoji = patternEmoji,
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
    patternEmoji: String,
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
            FigmaSignButton(emoji = patternEmoji, selected = true, onClick = {})
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
            fill = if (vibrationEnabled) FigmaFill.Light else FigmaFill.Power
        ) {
            FigmaPowerIcon(Modifier.size(18.dp))
        }
    }
}
