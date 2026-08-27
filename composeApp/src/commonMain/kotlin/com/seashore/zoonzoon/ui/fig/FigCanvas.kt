package com.seashore.zoonzoon.ui.fig

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.seashore.zoonzoon.generated.resources.Res
import com.seashore.zoonzoon.generated.resources.home_glow_hi
import com.seashore.zoonzoon.generated.resources.home_glow_lo
import org.jetbrains.compose.resources.imageResource
import kotlin.math.roundToInt

/** Width of the Figma frame `Home` (4028:2868). Every Figma value is in these units. */
val FigFrameWidth: Dp = 375.dp

/** Height of that frame. Only the mockup is this tall; real devices get [FigCanvas]'s value. */
val FigMockupHeight: Dp = 812.dp

/**
 * Maps the 375-wide Figma frame onto the device.
 *
 * The width is matched exactly, so horizontal proportions are identical to the mockup on any
 * screen. The height is *not* pinned to the mockup's 812: it is whatever the device gives,
 * expressed in frame units, and handed to [content] as `frameHeight`. Anchoring bottom elements
 * against that value keeps them on the bottom edge instead of letting them drift upwards on
 * screens that are proportionally taller than the mockup.
 */
@Composable
fun FigCanvas(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.(frameHeight: Dp) -> Unit
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(FigColor.black)
    ) {
        val scale = maxWidth / FigFrameWidth
        val frameHeight = maxHeight / scale
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    transformOrigin = TransformOrigin(0.5f, 0f)
                }
                .requiredSize(FigFrameWidth, frameHeight)
        ) {
            content(frameHeight)
        }
    }
}

/**
 * The two `Vector` glows behind every screen (4028:2869 / 4028:2870). They carry a 120-unit layer
 * blur, which a vector drawable cannot express, so they ship as the raster exports Figma produces
 * — already cropped to the frame's edges.
 *
 * Those exports bake in the frame's black backdrop and are fully opaque, so they are composited
 * with `Screen`. Over the black canvas that leaves the glow untouched while stopping the lower
 * export from painting a black rectangle across the upper one.
 */
@Composable
fun BoxScope.FigScreenGlow() {
    FigGlow(imageResource(Res.drawable.home_glow_hi), GlowHiHeight, 0.dp)
    FigGlow(
        image = imageResource(Res.drawable.home_glow_lo),
        height = GlowLoHeight,
        y = FigMockupHeight - GlowLoHeight
    )
}

@Composable
private fun FigGlow(image: ImageBitmap, height: Dp, y: Dp) {
    Canvas(
        modifier = Modifier
            .offset(y = y)
            .figNodeSize(FigFrameWidth, height)
    ) {
        drawImage(
            image = image,
            dstSize = IntSize(size.width.roundToInt(), size.height.roundToInt()),
            blendMode = BlendMode.Screen
        )
    }
}

private val GlowHiHeight = 632.6667.dp
private val GlowLoHeight = 458.3333.dp
