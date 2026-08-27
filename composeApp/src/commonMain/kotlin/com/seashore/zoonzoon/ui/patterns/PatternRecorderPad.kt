package com.seashore.zoonzoon.ui.patterns

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.seashore.zoonzoon.generated.resources.Res
import com.seashore.zoonzoon.generated.resources.patterns_pad_grid
import com.seashore.zoonzoon.ui.fig.FigColor
import com.seashore.zoonzoon.ui.fig.figNodeSize
import com.seashore.zoonzoon.ui.fig.figText
import kotlin.math.roundToInt
import org.jetbrains.compose.resources.imageResource

/** Figma pad `2097:12799` — 345×294, grid `2097:12801` is 344×244 at (1, 0). */
@Composable
fun PatternRecorderPad(
    hint: String,
    enabled: Boolean,
    onSample: (normalizedX: Float, normalizedY: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val grid = imageResource(Res.drawable.patterns_pad_grid)
    Box(modifier = modifier.size(345.dp, 294.dp)) {
        Canvas(
            modifier = Modifier
                .offset(x = 1.dp)
                .figNodeSize(344.dp, 244.dp)
                .pointerInput(enabled) {
                    if (!enabled) return@pointerInput
                    fun emit(position: Offset) {
                        val x = (position.x / size.width).coerceIn(0f, 1f)
                        val y = 1f - (position.y / size.height).coerceIn(0f, 1f)
                        onSample(x, y)
                    }
                    detectDragGestures(
                        onDragStart = { emit(it) },
                        onDrag = { change, _ ->
                            change.consume()
                            emit(change.position)
                        }
                    )
                }
        ) {
            drawImage(
                image = grid,
                dstSize = IntSize(size.width.roundToInt(), size.height.roundToInt()),
                blendMode = BlendMode.Screen
            )
        }
        Text(
            text = hint,
            color = FigColor.white.copy(alpha = 0.50f),
            style = figText(size = 14),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 264.dp)
        )
    }
}
