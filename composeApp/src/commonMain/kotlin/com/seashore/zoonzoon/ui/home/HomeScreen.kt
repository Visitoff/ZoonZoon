package com.seashore.zoonzoon.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.seashore.zoonzoon.gamepad.model.ConnectionState
import com.seashore.zoonzoon.gamepad.model.PresetPatterns
import com.seashore.zoonzoon.gamepad.viewmodel.GamepadViewModel
import com.seashore.zoonzoon.i18n.stringsFor
import com.seashore.zoonzoon.ui.components.ErrorDisplay

@Composable
fun HomeScreen(
    viewModel: GamepadViewModel,
    modifier: Modifier = Modifier
) {
    val connectionState by viewModel.connectionState.collectAsState()
    val vibrationState by viewModel.vibrationState.collectAsState()
    val errorState by viewModel.errorState.collectAsState()
    val showGamepadHelp by viewModel.showGamepadHelp.collectAsState()
    val showVibrationTargetPrompt by viewModel.showVibrationTargetPrompt.collectAsState()
    val language by viewModel.language.collectAsState()
    val activePatternKey by viewModel.activePatternKey.collectAsState()
    val strings = remember(language) { stringsFor(language) }

    val patternLabel = remember(activePatternKey, language) {
        PresetPatterns.labelForKey(activePatternKey, language)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(strings.appName, style = MaterialTheme.typography.headlineMedium)

        Text(
            text = connectionLabel(connectionState),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        ErrorDisplay(error = errorState)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(strings.vibrate, style = MaterialTheme.typography.titleMedium)
            Switch(
                checked = vibrationState.enabled,
                onCheckedChange = { viewModel.setVibrationEnabled(it) }
            )
        }

        Text(
            text = patternLabel,
            style = MaterialTheme.typography.bodyLarge
        )

        Text(strings.intensity, style = MaterialTheme.typography.titleSmall)
        Slider(
            value = vibrationState.intensity,
            onValueChange = { viewModel.setIntensity(it) },
            valueRange = 0f..1f
        )

        Text(strings.sharpness, style = MaterialTheme.typography.titleSmall)
        Slider(
            value = vibrationState.sharpness,
            onValueChange = { viewModel.setSharpness(it) },
            valueRange = 0f..1f
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(onClick = { viewModel.openGamepadHelp() }) {
                Text("Gamepad")
            }
            OutlinedButton(onClick = { viewModel.disconnect() }) {
                Text("Disconnect")
            }
        }

        Spacer(Modifier.height(8.dp))
    }

    if (showGamepadHelp) {
        GamepadConnectDialog(
            strings = strings,
            onDismiss = { viewModel.dismissGamepadHelp() }
        )
    }

    if (showVibrationTargetPrompt) {
        VibrationTargetDialog(
            strings = strings,
            onSelect = { viewModel.setVibrationTarget(it) },
            onDismiss = { viewModel.dismissVibrationTargetPrompt() }
        )
    }
}

private fun connectionLabel(state: ConnectionState): String = when (state) {
    ConnectionState.Disconnected -> "Controller: disconnected"
    ConnectionState.Scanning -> "Controller: scanning…"
    is ConnectionState.Connected -> "Controller: ${state.controllerType}"
}
