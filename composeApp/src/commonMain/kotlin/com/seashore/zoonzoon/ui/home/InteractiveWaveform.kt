package com.seashore.zoonzoon.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.sin

private val WaveCoral = Color(1.00f, 0.34f, 0.36f)
private val WavePink = Color(0.93f, 0.25f, 0.96f)
private val WaveViolet = Color(0.54f, 0.18f, 0.78f)

private val WaveGradientStops = arrayOf(
    0.00f to WaveCoral,
    0.62f to WavePink,
    1.00f to WaveViolet
)

/**
 * Live equalizer ribbons ported from the iOS `WaveformView`.
 *
 * Idle motion follows intensity and play/stop. A finger pull bends nearby
 * strings and reports normalized Y for left/right haptic mapping.
 */
@Composable
fun InteractiveWaveform(
    intensity: Float,
    active: Boolean,
    onTouchY: (Float?) -> Unit,
    modifier: Modifier = Modifier
) {
    val animator = remember { WaveformAnimator() }
    val ribbonPath = remember { Path() }
    val onTouchYState = rememberUpdatedState(onTouchY)
    var frameNanos by remember { mutableLongStateOf(0L) }

    SideEffect {
        animator.targetActivity = if (active) 1f else 0f
        animator.intensity = intensity.coerceIn(0f, 1f)
    }

    LaunchedEffect(Unit) {
        var lastNanos = 0L
        while (true) {
            withFrameNanos { now ->
                val delta = if (lastNanos == 0L) {
                    1f / 30f
                } else {
                    ((now - lastNanos) / 1_000_000_000f).coerceAtMost(1f / 15f)
                }
                lastNanos = now
                animator.advance(delta)
                frameNanos = now
            }
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    fun emit(position: Offset) {
                        val width = size.width.toFloat().coerceAtLeast(1f)
                        val height = size.height.toFloat().coerceAtLeast(1f)
                        val x = (position.x / width).coerceIn(0f, 1f)
                        val y = (position.y / height).coerceIn(0f, 1f)
                        animator.isInteracting = true
                        animator.interactionPointX = x
                        animator.interactionPointY = y
                        onTouchYState.value(y)
                    }
                    emit(down.position)
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.first()
                        if (!change.pressed) {
                            animator.isInteracting = false
                            onTouchYState.value(null)
                            break
                        }
                        change.consume()
                        emit(change.position)
                    }
                }
            }
    ) {
        frameNanos
        drawWaveform(animator, ribbonPath)
    }
}

internal class WaveformAnimator {
    var elapsedTime = 0f
    var activity = 0f
    var targetActivity = 0f
    var intensity = 0.55f
    var interactionPointX = 0.5f
    var interactionPointY = 0.5f
    var interactionAmount = 0f
    var isInteracting = false

    fun advance(deltaSeconds: Float) {
        val delta = deltaSeconds.coerceAtMost(1f / 15f)
        elapsedTime += delta
        val smoothing = 1f - exp(-delta * 5.5f)
        activity += (targetActivity - activity) * smoothing
        val interactionTarget = if (isInteracting) 1f else 0f
        val interactionSmoothing = 1f - exp(-delta * if (isInteracting) 14f else 3.8f)
        interactionAmount += (interactionTarget - interactionAmount) * interactionSmoothing
    }
}

private fun DrawScope.drawWaveform(animator: WaveformAnimator, ribbonPath: Path) {
    val density = density
    val width = size.width / density
    val height = size.height / density
    if (width <= 0f || height <= 0f) return

    val rowCount = 18
    val topInset = 8f
    val bottomInset = 26f
    val spacing = (height - topInset - bottomInset) / (rowCount - 1)
    val segments = 72
    val responsiveAmount = 0.12f + animator.activity * 0.88f
    val speed = 0.14f + animator.activity * (0.65f + animator.intensity * 0.90f)
    val displacement = 1.2f + responsiveAmount * (2.5f + animator.intensity * 8.5f)
    val thicknessResponse = responsiveAmount * (2.0f + animator.intensity * 8.0f)
    val twoPi = (PI * 2).toFloat()
    val touchX = animator.interactionPointX * width
    val touchY = animator.interactionPointY * height
    val horizontalReach = max(1f, width * 0.30f)
    val verticalReach = max(1f, spacing * 2.8f)
    val elapsed = animator.elapsedTime
    val intensity = animator.intensity
    val interactionAmount = animator.interactionAmount

    ribbonPath.reset()
    val upperX = FloatArray(segments + 1)
    val upperY = FloatArray(segments + 1)
    val lowerX = FloatArray(segments + 1)
    val lowerY = FloatArray(segments + 1)

    for (row in 0 until rowCount) {
        val centerY = topInset + row * spacing
        val rowPosition = row / (rowCount - 1).toFloat()
        val rowPhase = rowPosition * 2.15f
        val baseThickness = 5.5f + 1.4f * sin(rowPosition * PI.toFloat() * 3.2f)

        for (index in 0..segments) {
            val progress = index / segments.toFloat()
            val x = progress * width
            val travelingPhase = progress * twoPi * 0.92f - elapsed * speed
            val primaryWave = sin(travelingPhase + rowPhase)
            val secondaryWave = sin(
                progress * twoPi * 1.75f + elapsed * speed * 0.52f - rowPhase * 0.58f
            )
            val sharedFlow = primaryWave * 0.72f + secondaryWave * 0.28f
            var wave = sharedFlow * displacement

            val widthPulse = 0.5f + 0.5f * sin(
                progress * twoPi * 1.16f - elapsed * speed * 0.78f + rowPhase * 0.82f
            )
            val softPulse = widthPulse * widthPulse * (3f - 2f * widthPulse)
            var thickness = baseThickness + softPulse * thicknessResponse

            val horizontalDistance = kotlin.math.abs(x - touchX)
            val verticalDistance = kotlin.math.abs(centerY + wave - touchY)
            val reachRatio = horizontalDistance / horizontalReach
            val horizontalFalloff = exp(-reachRatio * reachRatio * 2.2f)
            val verticalFalloff = max(0f, 1f - verticalDistance / verticalReach)
            val stringInfluence = horizontalFalloff * verticalFalloff * interactionAmount
            val fingerPull = (touchY - centerY - wave) * stringInfluence * 0.18f
            val travelingRipple = sin(
                horizontalDistance * 0.075f - elapsed * (8.5f + intensity * 4.5f)
            ) * exp(-horizontalDistance / horizontalReach)
            wave += fingerPull + travelingRipple * stringInfluence * (3.0f + intensity * 4.0f)
            thickness += stringInfluence * (1.5f + intensity * 2.5f)

            upperX[index] = x
            upperY[index] = centerY + wave - thickness / 2f
            lowerX[index] = x
            lowerY[index] = centerY + wave + thickness / 2f
        }

        ribbonPath.moveTo(upperX[0] * density, upperY[0] * density)
        for (index in 1..segments) {
            ribbonPath.lineTo(upperX[index] * density, upperY[index] * density)
        }
        for (index in segments downTo 0) {
            ribbonPath.lineTo(lowerX[index] * density, lowerY[index] * density)
        }
        ribbonPath.close()
    }

    clipPath(ribbonPath) {
        drawRect(
            brush = Brush.horizontalGradient(
                colorStops = WaveGradientStops
            )
        )
    }
}
