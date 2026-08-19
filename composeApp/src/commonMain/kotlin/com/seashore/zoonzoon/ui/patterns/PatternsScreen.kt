package com.seashore.zoonzoon.ui.patterns

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.seashore.zoonzoon.gamepad.model.PatternReference
import com.seashore.zoonzoon.gamepad.model.PresetPatternId
import com.seashore.zoonzoon.gamepad.model.PresetPatterns
import com.seashore.zoonzoon.gamepad.viewmodel.GamepadViewModel
import com.seashore.zoonzoon.i18n.AppLanguage
import com.seashore.zoonzoon.i18n.stringsFor
import com.seashore.zoonzoon.ui.figma.FigmaBlack
import com.seashore.zoonzoon.ui.figma.FigmaScreenTitle
import com.seashore.zoonzoon.ui.figma.FigmaSelectableItem
import com.seashore.zoonzoon.ui.figma.FigmaTokens

@Composable
fun PatternsScreen(
    viewModel: GamepadViewModel,
    modifier: Modifier = Modifier
) {
    val language by viewModel.language.collectAsState()
    val activeKey by viewModel.activePatternKey.collectAsState()
    val recorderState by viewModel.recorderState.collectAsState()
    val strings = remember(language) { stringsFor(language) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(FigmaBlack)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = FigmaTokens.Spacing.s15, vertical = 54.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FigmaScreenTitle(text = strings.patterns)
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(PresetPatterns.all) { preset ->
                    val key = PatternReference.Preset(preset.id).key
                    FigmaSelectableItem(
                        emoji = preset.emoji,
                        title = if (language == AppLanguage.JA) preset.nameJa else preset.nameEn,
                        subtitle = if (language == AppLanguage.JA) preset.descriptionEn else preset.descriptionEn,
                        selected = viewModel.isPatternSelected(key),
                        onClick = { viewModel.selectPreset(preset.id) }
                    )
                }
            }
        }
    }

    if (recorderState.showSaveDialog) {
        SavePatternDialog(
            title = strings.savePatternTitle,
            nameHint = strings.patternNameHint,
            saveLabel = strings.save,
            cancelLabel = strings.cancel,
            onSave = viewModel::saveRecordedPattern,
            onDismiss = viewModel::dismissRecorderSaveDialog
        )
    }
}
