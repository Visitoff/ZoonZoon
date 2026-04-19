package com.seashore.zoonzoon.gamepad.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.seashore.zoonzoon.gamepad.model.ConnectionState
import com.seashore.zoonzoon.gamepad.viewmodel.GamepadViewModel

/**
 * Main screen for the Gamepad Vibration Controller.
 *
 * Layout (top → bottom):
 *  1. App title
 *  2. Error banner (only when error present)
 *  3. Connection status card + Scan button
 *  4. Vibration on/off control
 *  5. Pattern selection grid  ← always visible so user can pre-configure
 *  6. Intensity slider        ← always visible
 *  7. Custom pattern editor   ← shown when Custom pattern is selected
 *
 * **Validates: Requirements 7.1, 7.2, 7.9**
 */
@Composable
fun GamepadVibratorScreen(
    viewModel: GamepadViewModel,
    modifier: Modifier = Modifier
) {
    val connectionState by viewModel.connectionState.collectAsState()
    val vibrationState  by viewModel.vibrationState.collectAsState()
    val errorState      by viewModel.errorState.collectAsState()

    // Track whether custom editor is expanded
    var showCustomEditor by remember { mutableStateOf(false) }

    // Auto-show custom editor when Custom pattern is selected
    LaunchedEffect(vibrationState.activePattern) {
        showCustomEditor = vibrationState.activePattern is
                com.seashore.zoonzoon.gamepad.model.VibrationPattern.Custom
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── Title ──────────────────────────────────────────────────────────
        Text(
            text = "Gamepad Vibrator",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        // ── Error banner ───────────────────────────────────────────────────
        ErrorDisplay(error = errorState)

        // ── Connection status + scan button ────────────────────────────────
        ConnectionStatusCard(connectionState = connectionState)

        // Scan / Stop buttons depending on state
        when (connectionState) {
            is ConnectionState.Disconnected -> {
                Button(
                    onClick = { viewModel.startDiscovery() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Scan for Controllers")
                }
            }
            is ConnectionState.Scanning -> {
                OutlinedButton(
                    onClick = { viewModel.stopDiscovery() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Scanning… Tap to stop")
                }
            }
            is ConnectionState.Connected -> {
                OutlinedButton(
                    onClick = { viewModel.disconnect() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Disconnect")
                }
            }
        }

        // ── Vibration on/off ───────────────────────────────────────────────
        VibrationControlCard(
            vibrationState = vibrationState,
            onVibrationToggle = { viewModel.setVibrationEnabled(it) }
        )

        // ── Pattern selection ──────────────────────────────────────────────
        PatternSelectionCard(
            activePattern = vibrationState.activePattern,
            onPatternSelected = { viewModel.setPattern(it) }
        )

        // ── Intensity slider ───────────────────────────────────────────────
        IntensitySlider(
            intensity = vibrationState.intensity,
            onIntensityChanged = { viewModel.setIntensity(it) }
        )

        // ── Custom pattern editor (shown when Custom is active) ────────────
        if (showCustomEditor) {
            val customPattern = vibrationState.activePattern as?
                    com.seashore.zoonzoon.gamepad.model.VibrationPattern.Custom

            CustomPatternEditor(
                frames = customPattern?.frames ?: emptyList(),
                onFramesChanged = { frames ->
                    viewModel.saveAndApplyCustomPattern("current", frames, loop = true)
                }
            )
        }

        // ── Disconnected hint ──────────────────────────────────────────────
        if (connectionState is ConnectionState.Disconnected) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    text = "Connect a PlayStation or Xbox controller to send vibration commands.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}
