package com.seashore.zoonzoon.ui.fig

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.seashore.zoonzoon.ui.glass.GlassTintButton
import com.seashore.zoonzoon.ui.glass.isLiquidGlassEnabled
import com.seashore.zoonzoon.ui.glass.liquidGlass
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/**
 * Values below come from the Figma frame `Home` (4028:2868) of file
 * `z5m2GpAVF3qE2sDwV2MADs` and its component variables. Every dimension is expressed in the
 * frame's own units — see [FigCanvas] for how those map onto the device.
 */
object FigColor {
    val black = Color(0xFF000000)

    /** `-Player` / slider track fill. */
    val surface = Color(0xFF151515)

    /** Unfilled slider bar. */
    val barIdle = Color(0xFF212121)

    /** `button/fill-secondary-dark` = #33303533. */
    val buttonDark = Color(0x33333035)

    /** Power button and slider thumb. */
    val buttonSolid = Color(0xFF19181A)

    /** `-sign` outline. */
    val signBorder = Color(0xFFF4555C)

    /** `Shadow` behind the player caption, rgba(0,0,0,0.75). */
    val playerShadow = Color(0xBF000000)

    val white = Color(0xFFFFFFFF)
}

/** `coral/radial` — shared by the active nav pill and the filled slider bars. */
val FigCoralStops: Array<Pair<Float, Color>> = arrayOf(
    0.00f to Color(0xFFF1535E),
    0.25f to Color(0xFFEF5284),
    0.50f to Color(0xFFED51A9),
    1.00f to Color(0xFFE84FF4)
)

/**
 * The coral radial as Figma applies it to a pill: an ellipse anchored at the bottom centre whose
 * radius spans the full width of the node.
 */
fun Modifier.figCoralRadial(alpha: Float = 1f): Modifier = drawBehind {
    drawRect(
        brush = Brush.radialGradient(
            colorStops = FigCoralStops,
            center = Offset(size.width / 2f, size.height),
            radius = size.width
        ),
        alpha = alpha
    )
}

/** Same ramp on a vertical bar: coral at the bottom edge, violet at the top. */
fun Modifier.figCoralBar(): Modifier = drawBehind {
    drawRect(
        brush = Brush.verticalGradient(
            colorStops = FigCoralStops,
            startY = size.height,
            endY = 0f
        )
    )
}

/**
 * Draws an exported Figma asset at exactly the size it has in the frame, stretched to that box
 * rather than letter-boxed inside it — Figma sizes these nodes independently of their master's
 * aspect ratio, and the equalizer waves in particular are stretched well past theirs.
 */
@Composable
fun FigAsset(
    resource: DrawableResource,
    width: Dp,
    height: Dp,
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(resource),
        contentDescription = null,
        contentScale = ContentScale.FillBounds,
        modifier = modifier.figNodeSize(width, height)
    )
}

/**
 * Gives a node exactly the size it has in Figma even when that is larger than its parent, letting
 * the parent's clip do the cropping. `requiredSize` is not enough here: it still reports a size
 * coerced into the incoming constraints, which re-centres and squashes the overhang.
 */
fun Modifier.figNodeSize(width: Dp, height: Dp): Modifier = this
    .wrapContentSize(align = Alignment.TopStart, unbounded = true)
    .size(width, height)

/**
 * `Button (master)` — a circular pill. Figma expresses these as a fixed height plus symmetric
 * padding around the icon, which always resolves to a circle.
 */
@Composable
fun FigCircleButton(
    onClick: () -> Unit,
    diameter: Dp,
    modifier: Modifier = Modifier,
    fill: Color? = FigColor.buttonDark,
    coral: Boolean = false,
    borderColor: Color? = null,
    selected: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val glass = isLiquidGlassEnabled()
    val glassTint = when {
        fill != null && fill.alpha > 0f && fill.alpha < 0.99f -> fill
        coral -> Color.White.copy(alpha = 0.08f)
        fill == null || fill.alpha == 0f -> GlassTintButton
        else -> GlassTintButton
    }
    Box(
        modifier = modifier
            .size(diameter)
            .then(
                when {
                    glass -> Modifier
                        .liquidGlass(CircleShape, glassTint)
                        .then(if (coral) Modifier.figCoralRadial(alpha = 0.16f) else Modifier)
                    coral -> Modifier.clip(CircleShape).figCoralRadial()
                    fill != null -> Modifier.clip(CircleShape).background(fill)
                    else -> Modifier.clip(CircleShape)
                }
            )
            .then(
                if (borderColor != null) {
                    Modifier.border(1.dp, borderColor, CircleShape)
                } else {
                    Modifier
                }
            )
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            )
            .semantics { this.selected = selected },
        contentAlignment = Alignment.Center,
        content = content
    )
}
