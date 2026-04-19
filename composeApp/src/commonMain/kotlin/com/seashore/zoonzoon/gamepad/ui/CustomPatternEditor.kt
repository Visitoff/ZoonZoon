package com.seashore.zoonzoon.gamepad.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.seashore.zoonzoon.gamepad.model.VibrationPattern
import kotlin.math.roundToInt

/**
 * Custom pattern editor UI.
 *
 * Allows users to define custom vibration sequences by specifying
 * frame-by-frame motor values and durations.
 *
 * **Validates: Requirement 3.5**
 */
@Composable
fun CustomPatternEditor(
    frames: List<VibrationPattern.Custom.Frame>,
    onFramesChanged: (List<VibrationPattern.Custom.Frame>) -> Unit,
    modifier: Modifier = Modifier
) {
    var editableFrames by remember(frames) { mutableStateOf(frames) }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Custom Pattern Editor",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (editableFrames.isEmpty()) {
                Text(
                    text = "No frames yet. Add a frame to start.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(editableFrames) { index, frame ->
                        FrameEditor(
                            index = index,
                            frame = frame,
                            onFrameChanged = { updated ->
                                val newFrames = editableFrames.toMutableList()
                                newFrames[index] = updated
                                editableFrames = newFrames
                                onFramesChanged(newFrames)
                            },
                            onRemove = {
                                val newFrames = editableFrames.toMutableList()
                                newFrames.removeAt(index)
                                editableFrames = newFrames
                                onFramesChanged(newFrames)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    val newFrame = VibrationPattern.Custom.Frame(
                        leftMotor = 0.5f,
                        rightMotor = 0.5f,
                        durationMs = 200L
                    )
                    val newFrames = editableFrames + newFrame
                    editableFrames = newFrames
                    onFramesChanged(newFrames)
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("+ Add Frame")
            }
        }
    }
}

/**
 * Editor for a single custom pattern frame.
 */
@Composable
private fun FrameEditor(
    index: Int,
    frame: VibrationPattern.Custom.Frame,
    onFrameChanged: (VibrationPattern.Custom.Frame) -> Unit,
    onRemove: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Frame ${index + 1}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onRemove) {
                    Text("Remove", color = MaterialTheme.colorScheme.error)
                }
            }

            // Left motor slider
            Text(
                text = "Left Motor: ${(frame.leftMotor * 100).roundToInt()}%",
                style = MaterialTheme.typography.bodySmall
            )
            Slider(
                value = frame.leftMotor,
                onValueChange = { onFrameChanged(frame.copy(leftMotor = it)) },
                valueRange = 0f..1f
            )

            // Right motor slider
            Text(
                text = "Right Motor: ${(frame.rightMotor * 100).roundToInt()}%",
                style = MaterialTheme.typography.bodySmall
            )
            Slider(
                value = frame.rightMotor,
                onValueChange = { onFrameChanged(frame.copy(rightMotor = it)) },
                valueRange = 0f..1f
            )

            // Duration
            Text(
                text = "Duration: ${frame.durationMs}ms",
                style = MaterialTheme.typography.bodySmall
            )
            Slider(
                value = frame.durationMs.toFloat(),
                onValueChange = { onFrameChanged(frame.copy(durationMs = it.toLong())) },
                valueRange = 50f..2000f,
                steps = 38
            )
        }
    }
}
