package com.seashore.zoonzoon.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding
import com.seashore.zoonzoon.i18n.AppStrings
import com.seashore.zoonzoon.settings.VibrationTarget

@Composable
fun VibrationTargetDialog(
    strings: AppStrings,
    onSelect: (VibrationTarget) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.vibrationTargetTitle) },
        text = {
            Column {
                Text(strings.vibrationTargetBody, modifier = Modifier.padding(bottom = 8.dp))
                TextButton(onClick = { onSelect(VibrationTarget.PHONE_ONLY) }) {
                    Text(strings.phoneOnly)
                }
                TextButton(onClick = { onSelect(VibrationTarget.GAMEPAD_ONLY) }) {
                    Text(strings.gamepadOnly)
                }
                TextButton(onClick = { onSelect(VibrationTarget.GAMEPAD_AND_PHONE) }) {
                    Text(strings.gamepadAndPhone)
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.cancel)
            }
        }
    )
}
