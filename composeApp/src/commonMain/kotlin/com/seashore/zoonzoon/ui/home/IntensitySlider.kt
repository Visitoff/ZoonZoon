package com.seashore.zoonzoon.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.seashore.zoonzoon.generated.resources.Res
import com.seashore.zoonzoon.generated.resources.fig_slider_thumb
import com.seashore.zoonzoon.ui.figma.FigmaCoralEnd
import com.seashore.zoonzoon.ui.figma.FigmaCoralStart
import com.seashore.zoonzoon.ui.figma.FigmaTokens
import com.seashore.zoonzoon.ui.figma.figmaGilroy
import org.jetbrains.compose.resources.painterResource

private val SliderWidth = 345.dp
private val TrackHeight = 50.dp
private val TrackRadius = 100.dp
private val TrackBg = Color(0xFF151515)
private val BarInactive = Color(0xFF212121)
private val BarsAreaWidth = 339.dp
private val BarsAreaHeight = 44.dp
private val BarWidth = 4.dp
private val BarRadius = 45.dp
private val TrackPadding = 3.dp
private val ThumbSize = 50.dp

/** Figma Home `Slider` @ (15, 599): 345×82.692 — палочки + thumb + label. */
private val BarHeightsDp = intArrayOf(
    22, 36, 42,
    44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44,
    44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44,
    42, 36, 22
)

private val barGradient = Brush.verticalGradient(
    0f to FigmaCoralStart,
    1f to FigmaCoralEnd
)

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
                .background(TrackBg)
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
                    .requiredSize(BarsAreaWidth, BarsAreaHeight)
                    .offset(y = 0.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                val filledCount = (BarHeightsDp.size * t).toInt().coerceIn(0, BarHeightsDp.size)
                BarHeightsDp.forEachIndexed { index, height ->
                    val filled = index < filledCount
                    Box(
                        modifier = Modifier
                            .width(BarWidth)
                            .height(height.dp)
                            .clip(RoundedCornerShape(BarRadius))
                            .background(
                                if (filled) barGradient else Brush.linearGradient(
                                    0f to BarInactive,
                                    1f to BarInactive
                                )
                            )
                    )
                }
            }
            Image(
                painter = painterResource(Res.drawable.fig_slider_thumb),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .requiredSize(ThumbSize)
                    .align(Alignment.CenterStart)
                    .offset(x = (SliderWidth - ThumbSize) * t)
            )
        }
        Box(
            modifier = Modifier
                .width(129.321.dp)
                .height(32.692.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = FigmaTokens.Color.white.copy(alpha = 0.20f),
                style = figmaGilroy(size = 14, lineHeight = 10)
            )
        }
    }
}
