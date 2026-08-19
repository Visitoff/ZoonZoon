package com.seashore.zoonzoon.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.seashore.zoonzoon.generated.resources.Res
import com.seashore.zoonzoon.generated.resources.fig_eq
import com.seashore.zoonzoon.generated.resources.fig_intensity_body
import com.seashore.zoonzoon.generated.resources.fig_intensity_fill
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
private val PlayerHeight = 495.dp
private val ThumbTravel = 294.dp

/** Figma `-Player`: 345×495 at (15, 135), shell #151515 rounded 56. */
@Composable
fun Player(
    patternName: String,
    patternDescription: String,
    vibrationEnabled: Boolean,
    intensity: Float,
    intensityLabel: String,
    onToggleVibration: () -> Unit,
    onIntensityChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val t = intensity.coerceIn(0f, 1f)
    Box(modifier = modifier.requiredSize(PlayerWidth, PlayerHeight + 33.dp)) {
        Box(
            modifier = Modifier
                .requiredSize(PlayerWidth, PlayerHeight)
                .clip(RoundedCornerShape(FigmaTokens.Radius.r56))
                .background(FigmaTokens.Color.surface15)
        ) {
            Image(
                painter = painterResource(Res.drawable.fig_intensity_body),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .offset(y = 52.dp)
                    .size(PlayerWidth, 410.dp)
            )
            Image(
                painter = painterResource(Res.drawable.fig_intensity_fill),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .offset(y = 355.dp)
                    .size(51.dp + ThumbTravel * t, 107.dp)
            )
            Image(
                painter = painterResource(Res.drawable.fig_eq),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.size(PlayerWidth, 445.dp)
            )
            Equalizer(
                patternName = patternName,
                patternDescription = patternDescription,
                vibrationEnabled = vibrationEnabled,
                onToggleVibration = onToggleVibration,
                modifier = Modifier.offset(x = FigmaTokens.Spacing.s20, y = 365.dp)
            )
            Box(
                modifier = Modifier
                    .offset(y = 355.dp)
                    .size(PlayerWidth, 126.dp)
                    .pointerInput(onIntensityChanged) {
                        detectTapGestures { offset ->
                            onIntensityChanged((offset.x / size.width).coerceIn(0f, 1f))
                        }
                    }
                    .pointerInput(onIntensityChanged) {
                        detectHorizontalDragGestures { change, _ ->
                            change.consume()
                            onIntensityChanged((change.position.x / size.width).coerceIn(0f, 1f))
                        }
                    }
            )
            FigmaCircleButton(
                onClick = {},
                size = 51.dp,
                fill = FigmaFill.Coral,
                enabled = false,
                modifier = Modifier.offset(x = ThumbTravel * t, y = 411.dp)
            ) {
                IntensityChevrons()
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .size(130.dp, 33.dp)
                .clip(RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
                .background(FigmaTokens.Color.surface0E),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = intensityLabel,
                color = FigmaTokens.Color.white.copy(alpha = 0.20f),
                style = figmaGilroy(size = 14)
            )
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

@Composable
private fun IntensityChevrons() {
    Canvas(Modifier.size(20.dp, 9.dp)) {
        val stroke = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        fun chevron(originX: Float, alpha: Float) {
            val w = 9.dp.toPx()
            val h = 4.dp.toPx()
            val color = Color.White.copy(alpha)
            drawLine(color, Offset(originX, h), Offset(originX + w / 2f, 0f), stroke.width, cap = StrokeCap.Round)
            drawLine(color, Offset(originX + w / 2f, 0f), Offset(originX + w, h), stroke.width, cap = StrokeCap.Round)
        }
        chevron(0f, 0.2f)
        chevron(8.dp.toPx(), 0.5f)
        chevron(16.dp.toPx(), 1f)
    }
}
