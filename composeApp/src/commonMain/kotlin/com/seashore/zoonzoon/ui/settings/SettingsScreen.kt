package com.seashore.zoonzoon.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.seashore.zoonzoon.gamepad.viewmodel.GamepadViewModel
import com.seashore.zoonzoon.i18n.AppLanguage
import com.seashore.zoonzoon.i18n.stringsFor
import com.seashore.zoonzoon.settings.AppThemeMode
import com.seashore.zoonzoon.settings.VibrationTarget
import com.seashore.zoonzoon.ui.figma.FigmaBlack
import com.seashore.zoonzoon.ui.figma.FigmaScreenTitle
import com.seashore.zoonzoon.ui.figma.FigmaSectionTitle
import com.seashore.zoonzoon.ui.figma.FigmaSelectableItem
import com.seashore.zoonzoon.ui.figma.FigmaSelector
import com.seashore.zoonzoon.ui.figma.FigmaTokens

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
            .background(FigmaBlack)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = FigmaTokens.Spacing.s15, vertical = 54.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        FigmaScreenTitle(text = strings.settings)

        FigmaSectionTitle(text = strings.vibrationTarget)
        VibrationTarget.entries.forEach { target ->
            FigmaSelectableItem(
                emoji = "📳",
                title = vibrationTargetLabel(target, strings),
                subtitle = strings.vibrationTarget,
                selected = vibrationTarget == target,
                onClick = { viewModel.setVibrationTarget(target) },
                showRadio = true
            )
        }

        FigmaSectionTitle(text = strings.language)
        AppLanguage.entries.forEach { lang ->
            FigmaSelectableItem(
                emoji = "🌐",
                title = lang.displayName,
                subtitle = strings.language,
                selected = language == lang,
                onClick = { viewModel.setLanguage(lang) },
                showRadio = true
            )
        }

        FigmaSectionTitle(text = strings.theme)
        FigmaSelector(
            options = listOf(strings.themeLight, strings.themeDark, strings.themeSystem),
            selectedIndex = themeMode.ordinal,
            onSelected = { index ->
                viewModel.setThemeMode(AppThemeMode.entries[index])
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private fun vibrationTargetLabel(target: VibrationTarget, strings: com.seashore.zoonzoon.i18n.AppStrings): String =
    when (target) {
        VibrationTarget.PHONE_ONLY -> strings.phoneOnly
        VibrationTarget.GAMEPAD_ONLY -> strings.gamepadOnly
        VibrationTarget.GAMEPAD_AND_PHONE -> strings.gamepadAndPhone
    }
