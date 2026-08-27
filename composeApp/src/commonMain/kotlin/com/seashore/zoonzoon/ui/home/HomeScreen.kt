package com.seashore.zoonzoon.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.seashore.zoonzoon.gamepad.model.PatternPlayMode
import com.seashore.zoonzoon.gamepad.model.PatternReference
import com.seashore.zoonzoon.gamepad.model.PresetPatterns
import com.seashore.zoonzoon.gamepad.viewmodel.GamepadViewModel
import com.seashore.zoonzoon.i18n.AppLanguage
import com.seashore.zoonzoon.i18n.AppStrings
import com.seashore.zoonzoon.i18n.stringsFor

@Composable
fun HomeScreen(
    viewModel: GamepadViewModel,
    modifier: Modifier = Modifier,
    showStage: Boolean = true,
    showChrome: Boolean = true
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

    if (showStage) {
        Box(modifier = modifier.fillMaxSize()) {
            Player(
                patternName = patternCopy.name,
                patternDescription = patternCopy.description,
                patternEmoji = patternCopy.emoji,
                onToggleVibration = viewModel::toggleVibration,
                modifier = Modifier.offset(x = PlayerX, y = PlayerY)
            )
            IntensitySlider(
                intensity = vibrationState.intensity,
                label = strings.intensity,
                onIntensityChanged = viewModel::setIntensity,
                modifier = Modifier.offset(x = SliderX, y = SliderY)
            )
        }
    }

    if (showChrome) {
        TopBar(
            onLockClick = viewModel::openGamepadHelp,
            onAddClick = viewModel::openGamepadHelp,
            modifier = Modifier.offset(x = TopBarX, y = TopBarY)
        )
    }

    if (showChrome && showGamepadHelp) {
        GamepadConnectDialog(strings = strings, onDismiss = viewModel::dismissGamepadHelp)
    }

    if (showChrome && showTargetPrompt) {
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
