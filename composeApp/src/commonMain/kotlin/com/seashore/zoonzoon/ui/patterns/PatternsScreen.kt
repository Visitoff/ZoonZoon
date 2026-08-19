package com.seashore.zoonzoon.ui.patterns

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.seashore.zoonzoon.gamepad.model.PatternPlayMode
import com.seashore.zoonzoon.gamepad.model.PresetPatternId
import com.seashore.zoonzoon.gamepad.model.PresetPatterns
import com.seashore.zoonzoon.gamepad.viewmodel.GamepadViewModel
import com.seashore.zoonzoon.i18n.AppLanguage
import com.seashore.zoonzoon.i18n.stringsFor

private enum class PatternsTab { Presets, Custom, Playlist }

@Composable
fun PatternsScreen(
    viewModel: GamepadViewModel,
    modifier: Modifier = Modifier
) {
    val language by viewModel.language.collectAsState()
    val strings = remember(language) { stringsFor(language) }
    var tab by rememberSaveable { mutableStateOf(PatternsTab.Presets) }

    Column(modifier = modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = tab.ordinal) {
            Tab(
                selected = tab == PatternsTab.Presets,
                onClick = { tab = PatternsTab.Presets },
                text = { Text(strings.presets) }
            )
            Tab(
                selected = tab == PatternsTab.Custom,
                onClick = { tab = PatternsTab.Custom },
                text = { Text(strings.customRecorder) }
            )
            Tab(
                selected = tab == PatternsTab.Playlist,
                onClick = { tab = PatternsTab.Playlist },
                text = { Text(strings.playlist) }
            )
        }

        when (tab) {
            PatternsTab.Presets -> PresetsTab(viewModel, language, strings)
            PatternsTab.Custom -> CustomTab(viewModel, strings)
            PatternsTab.Playlist -> PlaylistTab(viewModel, language, strings)
        }
    }
}

@Composable
private fun PresetsTab(
    viewModel: GamepadViewModel,
    language: AppLanguage,
    strings: com.seashore.zoonzoon.i18n.AppStrings
) {
    val activePatternKey by viewModel.activePatternKey.collectAsState()
    val playMode by viewModel.playMode.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(PresetPatterns.all) { preset ->
            val key = "preset:${preset.id.key}"
            val selected = playMode == PatternPlayMode.SINGLE && activePatternKey == key
            FilterChip(
                selected = selected,
                onClick = { viewModel.selectPreset(preset.id) },
                label = {
                    Text(
                        "${preset.emoji} ${
                            if (language == AppLanguage.JA) preset.nameJa else preset.nameEn
                        }"
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun CustomTab(
    viewModel: GamepadViewModel,
    strings: com.seashore.zoonzoon.i18n.AppStrings
) {
    val customPatterns by viewModel.customPatterns.collectAsState()
    val recorderState by viewModel.recorderState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(strings.recorderHint, style = MaterialTheme.typography.bodySmall)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .pointerInput(recorderState.isRecording) {
                    if (!recorderState.isRecording) return@pointerInput
                    detectDragGestures { change, _ ->
                        change.consume()
                        val x = change.position.x / size.width
                        val y = 1f - (change.position.y / size.height)
                        viewModel.addRecorderSample(x, y)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                if (recorderState.isRecording) "Recording…" else strings.startRecording,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (!recorderState.isRecording) {
                Button(onClick = { viewModel.startRecorder() }) {
                    Text(strings.startRecording)
                }
            } else {
                Button(onClick = { viewModel.finishRecorder() }) {
                    Text(strings.finishRecording)
                }
            }
        }

        Text(strings.myPatterns, style = MaterialTheme.typography.titleMedium)
        customPatterns.forEach { entry ->
            val key = "custom:${entry.name}"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = viewModel.isPatternSelected(key),
                    onClick = { viewModel.selectCustomPattern(entry.name) },
                    label = { Text(entry.name) }
                )
                OutlinedButton(onClick = { viewModel.deleteCustomPattern(entry.name) }) {
                    Text(strings.delete)
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
            onSave = { viewModel.saveRecordedPattern(it) },
            onDismiss = { viewModel.dismissRecorderSaveDialog() }
        )
    }
}

@Composable
private fun PlaylistTab(
    viewModel: GamepadViewModel,
    language: AppLanguage,
    strings: com.seashore.zoonzoon.i18n.AppStrings
) {
    val playMode by viewModel.playMode.collectAsState()
    val queue by viewModel.playlistQueue.collectAsState()
    val savedPlaylists by viewModel.savedPlaylists.collectAsState()
    var savePlaylistOpen by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = playMode == PatternPlayMode.SINGLE,
                onClick = { viewModel.setPlayMode(PatternPlayMode.SINGLE) },
                label = { Text(strings.singleMode) }
            )
            FilterChip(
                selected = playMode == PatternPlayMode.PLAYLIST,
                onClick = { viewModel.setPlayMode(PatternPlayMode.PLAYLIST) },
                label = { Text(strings.playlistMode) }
            )
        }

        Text(strings.playlist, style = MaterialTheme.typography.titleMedium)
        queue.forEachIndexed { index, item ->
            var durationSec by remember(item.key, item.durationMs) {
                mutableIntStateOf((item.durationMs / 1000).toInt())
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    PresetPatterns.labelForKey(item.key, language),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = durationSec.toString(),
                    onValueChange = {
                        durationSec = it.toIntOrNull() ?: durationSec
                        viewModel.setPlaylistItemDuration(index, durationSec * 1000L)
                    },
                    label = { Text(strings.durationSeconds) },
                    modifier = Modifier.weight(0.6f),
                    singleLine = true
                )
                OutlinedButton(onClick = { viewModel.movePlaylistItem(index, -1) }) {
                    Text(strings.moveUp)
                }
                OutlinedButton(onClick = { viewModel.movePlaylistItem(index, 1) }) {
                    Text(strings.moveDown)
                }
                OutlinedButton(onClick = { viewModel.removeFromPlaylistAt(index) }) {
                    Text(strings.remove)
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { viewModel.clearPlaylist() }) {
                Text(strings.clearQueue)
            }
            Button(onClick = { savePlaylistOpen = true }, enabled = queue.isNotEmpty()) {
                Text(strings.savePlaylist)
            }
        }

        Text("Saved", style = MaterialTheme.typography.titleMedium)
        savedPlaylists.forEach { playlist ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(onClick = { viewModel.loadPlaylist(playlist.name) }) {
                    Text(playlist.name)
                }
                OutlinedButton(onClick = { viewModel.deleteSavedPlaylist(playlist.name) }) {
                    Text(strings.delete)
                }
            }
        }

        Text(strings.addToPlaylist, style = MaterialTheme.typography.titleMedium)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items(PresetPatternId.entries) { id ->
                OutlinedButton(
                    onClick = { viewModel.addToPlaylist("preset:${id.key}") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(PresetPatterns.definitionFor(id).nameEn)
                }
            }
        }
    }

    if (savePlaylistOpen) {
        SavePlaylistDialog(
            title = strings.savePlaylist,
            nameHint = strings.patternNameHint,
            saveLabel = strings.save,
            cancelLabel = strings.cancel,
            onSave = { viewModel.saveCurrentQueueAsPlaylist(it) },
            onDismiss = { savePlaylistOpen = false }
        )
    }
}
