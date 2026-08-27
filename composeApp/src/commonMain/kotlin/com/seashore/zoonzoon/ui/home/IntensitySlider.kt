package com.seashore.zoonzoon.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.seashore.zoonzoon.generated.resources.Res
import com.seashore.zoonzoon.generated.resources.home_intensity_notch
import com.seashore.zoonzoon.generated.resources.ic_arrows
import com.seashore.zoonzoon.ui.fig.FigAsset
import com.seashore.zoonzoon.ui.fig.FigColor
import com.seashore.zoonzoon.ui.fig.FigCoralStops
import com.seashore.zoonzoon.ui.fig.figText
import com.seashore.zoonzoon.ui.glass.GlassTintLight
import com.seashore.zoonzoon.ui.glass.ProvideGlassBackdrop
import com.seashore.zoonzoon.ui.glass.glassSource
import com.seashore.zoonzoon.ui.glass.isLiquidGlassEnabled
import com.seashore.zoonzoon.ui.glass.liquidGlass

/** Figma `Slider` (4028:2873): 345×82.6924 at (11, 599). */
val SliderWidth = 345.dp
val SliderHeight = 82.6924.dp
val SliderX = 11.dp
val SliderY = 599.dp

private val TrackHeight = 50.dp
private val TrackRadius = 100.dp
private val ThumbSize = 50.dp

/** `Frame 2043688368` — the bar strip, inset by 3 on both axes inside the track. */
private val BarsInset = 3.dp
private val BarsHeight = 44.dp
private val BarWidth = 4.dp
private val BarRadius = 45.dp
private val BarStep = 6.979167.dp

private const val BarCount = 49

private val NotchWidth = 129.321.dp
private val NotchHeight = 32.6924.dp
private val NotchX = 107.83936.dp

/** `Intensity` (4028:2980) sits at y=11 inside the notch. */
private val NotchLabelY = 11.dp

private fun barHeight(index: Int): Dp = when (index) {
    0, 48 -> 22.dp
    1, 47 -> 36.dp
    2, 46 -> 42.dp
    else -> 44.dp
}

private fun barX(index: Int): Dp = BarStep * index

@Composable
fun IntensitySlider(
    intensity: Float,
    label: String,
    onIntensityChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val fraction = intensity.coerceIn(0f, 1f)
    val travel = SliderWidth - ThumbSize
    val thumbX = travel * fraction
    val density = LocalDensity.current
    val onChange = rememberUpdatedState(onIntensityChanged)

    val travelPx = with(density) { travel.toPx() }
    val halfThumbPx = with(density) { (ThumbSize / 2).toPx() }
    val positionToFraction: (Float) -> Float = { x ->
        ((x - halfThumbPx) / travelPx).coerceIn(0f, 1f)
    }

    Box(modifier = modifier.size(SliderWidth, SliderHeight)) {
        ProvideGlassBackdrop {
            Box(
                modifier = Modifier
                    .size(SliderWidth, TrackHeight)
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            onChange.value(positionToFraction(offset.x))
                        }
                    }
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures { change, _ ->
                            onChange.value(positionToFraction(change.position.x))
                        }
                    }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(TrackRadius))
                        .background(FigColor.surface)
                        .glassSource()
                ) {
                    Bars(fillRight = thumbX + ThumbSize / 2)
                }
                Box(
                    modifier = Modifier
                        .offset(x = thumbX)
                        .size(ThumbSize)
                        .then(
                            if (isLiquidGlassEnabled()) {
                                Modifier.liquidGlass(CircleShape, GlassTintLight)
                            } else {
                                Modifier
                                    .clip(CircleShape)
                                    .background(FigColor.buttonSolid)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    FigAsset(
                        resource = Res.drawable.ic_arrows,
                        width = 17.5.dp,
                        height = 9.25003.dp
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .offset(x = NotchX, y = TrackHeight)
                .size(NotchWidth, NotchHeight)
        ) {
            FigAsset(
                resource = Res.drawable.home_intensity_notch,
                width = NotchWidth,
                height = NotchHeight
            )
            Text(
                text = label,
                color = FigColor.white.copy(alpha = 0.20f),
                style = figText(size = 14),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = NotchLabelY)
            )
        }
    }
}

/**
 * The 49 bars are drawn twice: once unfilled, then again with the coral ramp clipped to
 * [fillRight], which is how Figma layers the filled state over the empty one.
 */
@Composable
private fun Bars(fillRight: Dp, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val originX = BarsInset.toPx()
        val originY = BarsInset.toPx()
        val areaHeight = BarsHeight.toPx()
        val barWidthPx = BarWidth.toPx()
        val corner = CornerRadius(BarRadius.toPx())

        fun drawBars(coral: Boolean) {
            for (index in 0 until BarCount) {
                val heightPx = barHeight(index).toPx()
                val left = originX + barX(index).toPx()
                val top = originY + (areaHeight - heightPx) / 2f
                if (coral) {
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colorStops = FigCoralStops,
                            startY = top + heightPx,
                            endY = top
                        ),
                        topLeft = Offset(left, top),
                        size = Size(barWidthPx, heightPx),
                        cornerRadius = corner
                    )
                } else {
                    drawRoundRect(
                        color = FigColor.barIdle,
                        topLeft = Offset(left, top),
                        size = Size(barWidthPx, heightPx),
                        cornerRadius = corner
                    )
                }
            }
        }

        drawBars(coral = false)
        val right = fillRight.toPx()
        if (right > 0f) {
            clipRect(left = 0f, top = 0f, right = right, bottom = size.height) {
                drawBars(coral = true)
            }
        }
    }
}
