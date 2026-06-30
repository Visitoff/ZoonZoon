package com.seashore.zoonzoon.ui.patterns

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.seashore.zoonzoon.gamepad.model.PatternPlayMode
import com.seashore.zoonzoon.gamepad.model.PatternReference
import com.seashore.zoonzoon.gamepad.model.PresetPatterns
import com.seashore.zoonzoon.gamepad.theme.ZoonZoonAccentEnd
import com.seashore.zoonzoon.gamepad.theme.ZoonZoonAccentStart
import com.seashore.zoonzoon.gamepad.viewmodel.GamepadViewModel
import com.seashore.zoonzoon.i18n.AppLanguage
import com.seashore.zoonzoon.i18n.AppStrings
import com.seashore.zoonzoon.i18n.stringsFor
import com.seashore.zoonzoon.ui.components.IosAccentButton
import com.seashore.zoonzoon.ui.components.IosDestructiveTextButton
import com.seashore.zoonzoon.ui.components.IosGroupedCard
import com.seashore.zoonzoon.ui.components.IosGroupedDivider
import com.seashore.zoonzoon.ui.components.IosLargeTitle
import com.seashore.zoonzoon.ui.components.IosListRow
import com.seashore.zoonzoon.ui.components.IosScreenBackground
import com.seashore.zoonzoon.ui.components.IosSecondaryButton
import com.seashore.zoonzoon.ui.components.IosSectionHeader
import com.seashore.zoonzoon.ui.components.IosSegmentedControl
import kotlin.math.roundToInt

@Composable
fun PatternsScreen(
    viewModel: GamepadViewModel,
    modifier: Modifier = Modifier
) {
    val language by viewModel.language.collectAsState()
    val strings = remember(language) { stringsFor(language) }
    val playMode by viewModel.playMode.collectAsState()
    val activeKey by viewModel.activePatternKey.collectAsState()
    val customPatterns by viewModel.customPatterns.collectAsState()
    val playlistQueue by viewModel.playlistQueue.collectAsState()
    val savedPlaylists by viewModel.savedPlaylists.collectAsState()
    val recorderState by viewModel.recorderState.collectAsState()
    var showSavePlaylistDialog by remember { mutableStateOf(false) }

    IosScreenBackground(modifier = modifier) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            item {
                IosLargeTitle(text = strings.patterns)
            }

            item {
                IosGroupedCard(modifier = Modifier.padding(top = 8.dp)) {
                    IosSegmentedControl(
                        options = listOf(strings.singleMode, strings.playlistMode),
                        selectedIndex = if (playMode == PatternPlayMode.SINGLE) 0 else 1,
                        onSelected = { index ->
                            viewModel.setPlayMode(
                                if (index == 0) PatternPlayMode.SINGLE else PatternPlayMode.PLAYLIST
                            )
                        },
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            item { IosSectionHeader(text = strings.presets) }

            item {
                IosGroupedCard {
                    PresetPatterns.all.forEachIndexed { index, preset ->
                        val key = PatternReference.Preset(preset.id).key
                        val title = if (language == AppLanguage.JA) preset.nameJa else preset.nameEn
                        val subtitle = "${preset.emoji} · ${preset.descriptionEn}"
                        IosListRow(
                            title = title,
                            subtitle = subtitle,
                            selected = playMode == PatternPlayMode.SINGLE && activeKey == key,
                            onClick = { viewModel.selectPreset(preset.id) },
                            trailing = {
                                IosSecondaryButton(
                                    text = "+",
                                    onClick = { viewModel.addToPlaylist(key) },
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        )
                        if (index < PresetPatterns.all.lastIndex) {
                            IosGroupedDivider()
                        }
                    }
                }
            }

            item { IosSectionHeader(text = strings.customRecorder) }

            item {
                IosGroupedCard {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (!recorderState.isRecording) {
                            IosAccentButton(
                                text = strings.startRecording,
                                onClick = viewModel::startRecorder,
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            IosSecondaryButton(
                                text = strings.finishRecording,
                                onClick = viewModel::finishRecorder,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        PatternRecorderPad(
                            isRecording = recorderState.isRecording,
                            onSample = viewModel::addRecorderSample,
                            onFinish = viewModel::finishRecorder,
                            hint = strings.recorderHint
                        )
                    }
                }
            }

            if (customPatterns.isNotEmpty()) {
                item { IosSectionHeader(text = strings.myPatterns) }
                item {
                    IosGroupedCard {
                        customPatterns.forEachIndexed { index, entry ->
                            val key = PatternReference.Custom(entry.name).key
                            IosListRow(
                                title = entry.name,
                                subtitle = "${entry.pattern.samples.size} samples",
                                selected = playMode == PatternPlayMode.SINGLE && activeKey == key,
                                onClick = { viewModel.selectCustomPattern(entry.name) },
                                trailing = {
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        IosSecondaryButton(
                                            text = "+",
                                            onClick = { viewModel.addToPlaylist(key) }
                                        )
                                        IosSecondaryButton(
                                            text = strings.delete,
                                            onClick = { viewModel.deleteCustomPattern(entry.name) }
                                        )
                                    }
                                }
                            )
                            if (index < customPatterns.lastIndex) {
                                IosGroupedDivider()
                            }
                        }
                    }
                }
            }

            item { IosSectionHeader(text = strings.playlist) }

            if (playlistQueue.isEmpty()) {
                item {
                    IosGroupedCard {
                        Text(
                            text = strings.clearQueue,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                itemsIndexed(playlistQueue) { index, item ->
                    IosGroupedCard(modifier = Modifier.padding(bottom = 8.dp)) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = patternLabel(item.key, language),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${strings.durationSeconds}: ${(item.durationMs / 1000f).roundToInt()}s",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Slider(
                                value = (item.durationMs / 1000f).coerceIn(1f, 60f),
                                onValueChange = { sec ->
                                    viewModel.setPlaylistItemDuration(index, (sec * 1000).roundToInt().toLong())
                                },
                                valueRange = 1f..60f,
                                steps = 58,
                                colors = SliderDefaults.colors(
                                    thumbColor = ZoonZoonAccentStart,
                                    activeTrackColor = ZoonZoonAccentStart,
                                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                IosSecondaryButton(text = strings.moveUp, onClick = { viewModel.movePlaylistItem(index, -1) }, modifier = Modifier.weight(1f))
                                IosSecondaryButton(text = strings.moveDown, onClick = { viewModel.movePlaylistItem(index, 1) }, modifier = Modifier.weight(1f))
                                IosSecondaryButton(text = strings.remove, onClick = { viewModel.removeFromPlaylistAt(index) }, modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            item {
                IosGroupedCard(modifier = Modifier.padding(top = 8.dp)) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            IosAccentButton(
                                text = strings.playPlaylist,
                                onClick = {
                                    viewModel.setPlayMode(PatternPlayMode.PLAYLIST)
                                    viewModel.setVibrationEnabled(true)
                                },
                                modifier = Modifier.weight(1f),
                                enabled = playlistQueue.isNotEmpty()
                            )
                            IosSecondaryButton(
                                text = strings.savePlaylist,
                                onClick = { showSavePlaylistDialog = true },
                                modifier = Modifier.weight(1f),
                                enabled = playlistQueue.isNotEmpty()
                            )
                        }
                        IosDestructiveTextButton(
                            text = strings.clearQueue,
                            onClick = viewModel::clearPlaylist
                        )
                    }
                }
            }

            if (savedPlaylists.isNotEmpty()) {
                item { IosSectionHeader(text = strings.savePlaylist) }
                item {
                    IosGroupedCard {
                        savedPlaylists.forEachIndexed { index, playlist ->
                            IosListRow(
                                title = playlist.name,
                                subtitle = "${playlist.items.size} items",
                                onClick = { viewModel.loadPlaylist(playlist.name) },
                                trailing = {
                                    IosSecondaryButton(
                                        text = strings.delete,
                                        onClick = { viewModel.deleteSavedPlaylist(playlist.name) }
                                    )
                                }
                            )
                            if (index < savedPlaylists.lastIndex) {
                                IosGroupedDivider()
                            }
                        }
                    }
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

    if (showSavePlaylistDialog) {
        SavePlaylistDialog(
            title = strings.savePlaylist,
            nameHint = strings.patternNameHint,
            saveLabel = strings.save,
            cancelLabel = strings.cancel,
            onSave = { name ->
                viewModel.saveCurrentQueueAsPlaylist(name)
                showSavePlaylistDialog = false
            },
            onDismiss = { showSavePlaylistDialog = false }
        )
    }
}

private fun patternLabel(key: String, language: AppLanguage): String {
    val ref = PatternReference.fromKey(key) ?: return key
    return when (ref) {
        is PatternReference.Preset -> {
            val def = PresetPatterns.definitionFor(ref.id)
            if (language == AppLanguage.JA) def.nameJa else def.nameEn
        }
        is PatternReference.Custom -> ref.name
    }
}
