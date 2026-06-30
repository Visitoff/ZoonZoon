package com.seashore.zoonzoon.ui.patterns

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun SavePatternDialog(
    title: String,
    nameHint: String,
    saveLabel: String,
    cancelLabel: String,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(nameHint) },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(onClick = { onSave(name.trim()) }) {
                Text(saveLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(cancelLabel)
            }
        }
    )
}

@Composable
fun SavePlaylistDialog(
    title: String,
    nameHint: String,
    saveLabel: String,
    cancelLabel: String,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(nameHint) },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(onClick = { onSave(name.trim()) }) {
                Text(saveLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(cancelLabel)
            }
        }
    )
}
