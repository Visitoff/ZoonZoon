package com.seashore.zoonzoon.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.seashore.zoonzoon.ui.figma.FigmaArrowsIcon
import com.seashore.zoonzoon.ui.figma.FigmaGradients
import com.seashore.zoonzoon.ui.figma.FigmaIntensityNotch
import com.seashore.zoonzoon.ui.figma.FigmaTokens
import com.seashore.zoonzoon.ui.figma.figmaGilroy

private val SliderWidth = 345.dp
private val TrackHeight = 50.dp
private val TrackRadius = 100.dp
private val BarsAreaWidth = 339.dp
private val BarsAreaHeight = 44.dp
private val BarWidth = 4.dp
private val BarRadius = 45.dp
private val ThumbSize = 50.dp

private val BarHeightsDp = intArrayOf(
    22, 36, 42
) + IntArray(42) { 44 } + intArrayOf(42, 36, 22)

@Composable
private fun IntensityBar(heightDp: Int, filled: Boolean) {
    Box(
        modifier = Modifier
            .width(BarWidth)
            .height(heightDp.dp)
            .clip(RoundedCornerShape(BarRadius))
            .background(
                if (filled) {
                    FigmaGradients.intensityBarBrush(heightDp.toFloat())
                } else {
                    Brush.linearGradient(
                        0f to FigmaTokens.Color.barInactive,
                        1f to FigmaTokens.Color.barInactive
                    )
                }
            )
    )
}

/** Figma `Slider` (4028:2873): 345×82.692 @ y=599. */
@Composable
fun IntensitySlider(
    intensity: Float,
    label: String,
    onIntensityChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val t = intensity.coerceIn(0f, 1f)
    Column(
        modifier = modifier.requiredSize(SliderWidth, 82.692.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .requiredSize(SliderWidth, TrackHeight)
                .clip(RoundedCornerShape(TrackRadius))
                .background(FigmaTokens.Color.surface15)
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
        ) {
            Row(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(x = 3.dp, y = 3.dp)
                    .requiredSize(BarsAreaWidth, BarsAreaHeight),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                val filledCount = (BarHeightsDp.size * t).toInt().coerceIn(0, BarHeightsDp.size)
                BarHeightsDp.forEachIndexed { index, height ->
                    IntensityBar(height, index < filledCount)
                }
            }
            Box(
                modifier = Modifier
                    .requiredSize(ThumbSize)
                    .align(Alignment.CenterStart)
                    .offset(x = (SliderWidth - ThumbSize) * t)
                    .clip(CircleShape)
                    .background(FigmaTokens.Color.powerButton),
                contentAlignment = Alignment.Center
            ) {
                FigmaArrowsIcon(Modifier.requiredSize(18.dp))
            }
        }
        Box(
            modifier = Modifier.requiredSize(129.321.dp, 32.692.dp),
            contentAlignment = Alignment.Center
        ) {
            FigmaIntensityNotch(Modifier.fillMaxSize())
            Text(
                text = label,
                color = FigmaTokens.Color.white.copy(alpha = 0.20f),
                style = figmaGilroy(size = 14, lineHeight = 24)
            )
        }
    }
}
