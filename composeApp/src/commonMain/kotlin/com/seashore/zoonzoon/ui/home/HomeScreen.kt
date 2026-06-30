package com.seashore.zoonzoon.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.seashore.zoonzoon.gamepad.theme.LocalGlassTokens
import com.seashore.zoonzoon.gamepad.theme.ZoonZoonAccentStart
import com.seashore.zoonzoon.gamepad.theme.glassSurface
import com.seashore.zoonzoon.gamepad.ui.ErrorDisplay
import com.seashore.zoonzoon.gamepad.viewmodel.GamepadViewModel
import com.seashore.zoonzoon.i18n.stringsFor
import com.seashore.zoonzoon.ui.components.HomeIntensitySlider
import com.seashore.zoonzoon.ui.components.IosLargeTitle
import com.seashore.zoonzoon.ui.components.IosScreenBackground
import com.seashore.zoonzoon.ui.components.NeomorphicVibrateButton

@Composable
fun HomeScreen(
    viewModel: GamepadViewModel,
    modifier: Modifier = Modifier
) {
    val vibrationState by viewModel.vibrationState.collectAsState()
    val errorState by viewModel.errorState.collectAsState()
    val showGamepadHelp by viewModel.showGamepadHelp.collectAsState()
    val showTargetPrompt by viewModel.showVibrationTargetPrompt.collectAsState()
    val language by viewModel.language.collectAsState()
    val strings = remember(language) { stringsFor(language) }
    val connected = viewModel.isGamepadConnected
    val glassTokens = LocalGlassTokens.current

    IosScreenBackground(modifier = modifier, accentGlow = true) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, end = 16.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .clip(CircleShape)
                        .glassSurface(shape = CircleShape, tokens = glassTokens, elevation = 6.dp)
                        .then(
                            if (connected) Modifier else {
                                Modifier.clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = viewModel::openGamepadHelp
                                )
                            }
                        )
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                if (connected) Color(0xFF34C759) else ZoonZoonAccentStart
                            )
                    )
                    Text(
                        text = if (connected) "Controller" else "Connect",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (connected) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            ZoonZoonAccentStart
                        }
                    )
                }
            }

            IosLargeTitle(text = strings.appName)

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    ErrorDisplay(error = errorState)

                    NeomorphicVibrateButton(
                        enabled = vibrationState.enabled,
                        onClick = viewModel::toggleVibration,
                        offLabel = strings.vibrate,
                        onLabel = strings.on
                    )
                }
            }

            HomeIntensitySlider(
                intensity = vibrationState.intensity,
                onIntensityChanged = viewModel::setIntensity,
                label = strings.intensity,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }
    }

    if (showGamepadHelp) {
        GamepadConnectDialog(strings = strings, onDismiss = viewModel::dismissGamepadHelp)
    }

    if (showTargetPrompt) {
        VibrationTargetDialog(
            strings = strings,
            onSelect = viewModel::setVibrationTarget,
            onDismiss = viewModel::dismissVibrationTargetPrompt
        )
    }
}
