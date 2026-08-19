package com.seashore.zoonzoon.ui.patterns

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.seashore.zoonzoon.gamepad.theme.ZoonZoonAccentEnd
import com.seashore.zoonzoon.gamepad.theme.ZoonZoonAccentStart

@Composable
fun PatternRecorderPad(
    isRecording: Boolean,
    onSample: (normalizedX: Float, normalizedY: Float) -> Unit,
    onFinish: () -> Unit,
    hint: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                    )
                )
            )
            .border(
                width = 2.dp,
                color = if (isRecording) ZoonZoonAccentStart else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                shape = RoundedCornerShape(16.dp)
            )
            .then(
                if (isRecording) {
                    Modifier.pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                onSample(
                                    (offset.x / size.width).coerceIn(0f, 1f),
                                    (1f - offset.y / size.height).coerceIn(0f, 1f)
                                )
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                onSample(
                                    (change.position.x / size.width).coerceIn(0f, 1f),
                                    (1f - change.position.y / size.height).coerceIn(0f, 1f)
                                )
                            },
                            onDragEnd = { onFinish() },
                            onDragCancel = { onFinish() }
                        )
                    }
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = hint,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
            modifier = Modifier.padding(16.dp)
        )
    }
}
