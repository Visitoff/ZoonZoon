package com.seashore.zoonzoon.ui.home

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.seashore.zoonzoon.i18n.AppStrings

@Composable
fun GamepadConnectDialog(
    strings: AppStrings,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.connectGamepadTitle) },
        text = { Text(strings.connectGamepadBody) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.gotIt)
            }
        }
    )
}
