package com.seashore.zoonzoon.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.seashore.zoonzoon.gamepad.model.PatternPlayMode
import com.seashore.zoonzoon.gamepad.model.PatternReference
import com.seashore.zoonzoon.gamepad.model.PresetPatterns
import com.seashore.zoonzoon.gamepad.viewmodel.GamepadViewModel
import com.seashore.zoonzoon.i18n.AppLanguage
import com.seashore.zoonzoon.i18n.AppStrings
import com.seashore.zoonzoon.i18n.stringsFor

private val FrameCenterX = (HomeFrameWidth - 345.dp) / 2

@Composable
fun HomeScreen(
    viewModel: GamepadViewModel,
    modifier: Modifier = Modifier
) {
    val vibrationState by viewModel.vibrationState.collectAsState()
    val showGamepadHelp by viewModel.showGamepadHelp.collectAsState()
    val showTargetPrompt by viewModel.showVibrationTargetPrompt.collectAsState()
    val language by viewModel.language.collectAsState()
    val strings = remember(language) { stringsFor(language) }
    val activeKey by viewModel.activePatternKey.collectAsState()
    val playMode by viewModel.playMode.collectAsState()
    val patternCopy = remember(activeKey, playMode, language, strings) {
        homePatternCopy(activeKey, playMode, language, strings)
    }

    Box(modifier = modifier.requiredSize(HomeFrameWidth, HomeFrameHeight)) {
        TopBar(
            onLockClick = viewModel::openGamepadHelp,
            modifier = Modifier.offset(x = FrameCenterX, y = 79.dp)
        )
        Player(
            patternName = patternCopy.name,
            patternDescription = patternCopy.description,
            patternEmoji = patternCopy.emoji,
            vibrationEnabled = vibrationState.enabled,
            onToggleVibration = viewModel::toggleVibration,
            modifier = Modifier.offset(x = FrameCenterX, y = 135.dp)
        )
        IntensitySlider(
            intensity = vibrationState.intensity,
            label = strings.intensity,
            onIntensityChanged = viewModel::setIntensity,
            modifier = Modifier.offset(x = FrameCenterX, y = 599.dp)
        )
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

private data class HomePatternCopy(
    val name: String,
    val description: String,
    val emoji: String
)

private fun homePatternCopy(
    activeKey: String,
    playMode: PatternPlayMode,
    language: AppLanguage,
    strings: AppStrings
): HomePatternCopy {
    if (playMode == PatternPlayMode.PLAYLIST) {
        return HomePatternCopy(
            name = strings.playlist,
            description = strings.playlistMode,
            emoji = "💗"
        )
    }
    return when (val reference = PatternReference.fromKey(activeKey)) {
        is PatternReference.Preset -> {
            val def = PresetPatterns.definitionFor(reference.id)
            HomePatternCopy(
                name = PresetPatterns.labelForKey(activeKey, language),
                description = def.descriptionEn,
                emoji = def.emoji
            )
        }
        is PatternReference.Custom -> HomePatternCopy(
            name = reference.name.ifBlank { strings.customRecorder },
            description = strings.customRecorder,
            emoji = "✨"
        )
        null -> HomePatternCopy(
            name = strings.steady,
            description = strings.constantVibration,
            emoji = "▬"
        )
    }
}
