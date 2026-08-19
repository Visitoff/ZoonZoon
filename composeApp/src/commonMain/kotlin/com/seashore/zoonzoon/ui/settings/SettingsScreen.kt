package com.seashore.zoonzoon.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.seashore.zoonzoon.gamepad.viewmodel.GamepadViewModel
import com.seashore.zoonzoon.i18n.AppLanguage
import com.seashore.zoonzoon.i18n.stringsFor
import com.seashore.zoonzoon.platform.openStoreReviewPage
import com.seashore.zoonzoon.settings.AppThemeMode
import com.seashore.zoonzoon.settings.VibrationTarget

@Composable
fun SettingsScreen(
    viewModel: GamepadViewModel,
    modifier: Modifier = Modifier
) {
    val vibrationTarget by viewModel.vibrationTarget.collectAsState()
    val language by viewModel.language.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val strings = remember(language) { stringsFor(language) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(strings.settings, style = MaterialTheme.typography.headlineMedium)

        Text(strings.vibrationTarget, style = MaterialTheme.typography.titleMedium)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            VibrationTarget.entries.forEach { target ->
                FilterChip(
                    selected = vibrationTarget == target,
                    onClick = { viewModel.setVibrationTarget(target) },
                    label = { Text(vibrationTargetLabel(target, strings)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Text(strings.language, style = MaterialTheme.typography.titleMedium)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            AppLanguage.entries.forEach { lang ->
                FilterChip(
                    selected = language == lang,
                    onClick = { viewModel.setLanguage(lang) },
                    label = { Text(lang.displayName) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Text(strings.theme, style = MaterialTheme.typography.titleMedium)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            AppThemeMode.entries.forEach { mode ->
                FilterChip(
                    selected = themeMode == mode,
                    onClick = { viewModel.setThemeMode(mode) },
                    label = { Text(themeModeLabel(mode, strings)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Button(onClick = { openStoreReviewPage() }, modifier = Modifier.fillMaxWidth()) {
            Text(strings.leaveReview)
        }
    }
}

private fun vibrationTargetLabel(
    target: VibrationTarget,
    strings: com.seashore.zoonzoon.i18n.AppStrings
): String = when (target) {
    VibrationTarget.PHONE_ONLY -> strings.phoneOnly
    VibrationTarget.GAMEPAD_ONLY -> strings.gamepadOnly
    VibrationTarget.GAMEPAD_AND_PHONE -> strings.gamepadAndPhone
}

private fun themeModeLabel(
    mode: AppThemeMode,
    strings: com.seashore.zoonzoon.i18n.AppStrings
): String = when (mode) {
    AppThemeMode.LIGHT -> strings.themeLight
    AppThemeMode.DARK -> strings.themeDark
    AppThemeMode.SYSTEM -> strings.themeSystem
}
