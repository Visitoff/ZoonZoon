package com.seashore.zoonzoon.ui.patterns

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.seashore.zoonzoon.gamepad.model.PatternPlayMode
import com.seashore.zoonzoon.gamepad.model.PatternReference
import com.seashore.zoonzoon.gamepad.model.PresetPatterns
import com.seashore.zoonzoon.gamepad.viewmodel.GamepadViewModel
import com.seashore.zoonzoon.generated.resources.Res
import com.seashore.zoonzoon.generated.resources.fig_ic_plus
import com.seashore.zoonzoon.generated.resources.fig_ic_rec
import com.seashore.zoonzoon.generated.resources.fig_touch_area
import com.seashore.zoonzoon.i18n.AppLanguage
import com.seashore.zoonzoon.i18n.stringsFor
import com.seashore.zoonzoon.ui.figma.FigmaCircleButton
import com.seashore.zoonzoon.ui.figma.FigmaCoralStart
import com.seashore.zoonzoon.ui.figma.FigmaFill
import com.seashore.zoonzoon.ui.figma.FigmaPillButton
import com.seashore.zoonzoon.ui.figma.FigmaNestedSelectableItem
import com.seashore.zoonzoon.ui.figma.FigmaPlaylistCard
import com.seashore.zoonzoon.ui.figma.FigmaSelectableItem
import com.seashore.zoonzoon.ui.figma.FigmaSelector
import com.seashore.zoonzoon.ui.figma.FigmaTokens
import com.seashore.zoonzoon.ui.figma.figmaGilroy
import com.seashore.zoonzoon.ui.home.HomeFrameHeight
import com.seashore.zoonzoon.ui.home.HomeFrameWidth
import org.jetbrains.compose.resources.painterResource

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
    var tab by remember { mutableIntStateOf(0) }
    var showSavePlaylistDialog by remember { mutableStateOf(false) }
    var expandedPlaylist by remember { mutableStateOf<String?>(null) }

    Box(modifier = modifier.requiredSize(HomeFrameWidth, HomeFrameHeight)) {
        Column(
            modifier = Modifier
                .offset(x = FigmaTokens.Spacing.s15, y = 54.dp)
                .size(345.dp, 648.dp)
        ) {
            FigmaSelector(
                options = if (language == AppLanguage.JA) {
                    listOf(strings.patterns, strings.customRecorder, strings.playlist)
                } else {
                    listOf("Patterns", "Custom", "Playlists")
                },
                selectedIndex = tab,
                onSelected = { tab = it }
            )
            Box(Modifier.height(30.dp))
            when (tab) {
                0 -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 12.dp)
                ) {
                    items(PresetPatterns.all, key = { it.id.key }) { preset ->
                        val key = PatternReference.Preset(preset.id).key
                        FigmaSelectableItem(
                            emoji = preset.emoji,
                            title = if (language == AppLanguage.JA) preset.nameJa else preset.nameEn,
                            subtitle = preset.descriptionEn,
                            selected = playMode == PatternPlayMode.SINGLE && activeKey == key,
                            onClick = { viewModel.selectPreset(preset.id) },
                            onLongPress = { viewModel.addToPlaylist(key) }
                        )
                    }
                    items(customPatterns, key = { it.name }) { entry ->
                        val key = PatternReference.Custom(entry.name).key
                        FigmaSelectableItem(
                            emoji = "💗",
                            title = entry.name,
                            subtitle = strings.customRecorder,
                            selected = playMode == PatternPlayMode.SINGLE && activeKey == key,
                            onClick = { viewModel.selectCustomPattern(entry.name) },
                            onLongPress = { viewModel.deleteCustomPattern(entry.name) }
                        )
                    }
                }
                1 -> FigmaCustomTab(
                    isRecording = recorderState.isRecording,
                    hint = strings.recorderHint,
                    startLabel = strings.startRecording,
                    finishLabel = strings.finishRecording,
                    onToggle = {
                        if (recorderState.isRecording) viewModel.finishRecorder()
                        else viewModel.startRecorder()
                    },
                    onSample = viewModel::addRecorderSample
                )
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(15.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    if (playlistQueue.isNotEmpty()) {
                        item(key = "queue-card") {
                            FigmaPlaylistCard(
                                title = strings.playlist,
                                subtitle = strings.playlistMode,
                                expanded = expandedPlaylist == "__queue__",
                                onHeaderClick = {
                                    expandedPlaylist = if (expandedPlaylist == "__queue__") null else "__queue__"
                                    viewModel.setPlayMode(PatternPlayMode.PLAYLIST)
                                    viewModel.setVibrationEnabled(true)
                                }
                            ) {
                                playlistQueue.forEachIndexed { index, item ->
                                    FigmaNestedSelectableItem(
                                        emoji = "💗",
                                        title = patternLabel(item.key, language),
                                        subtitle = strings.playlist,
                                        onClick = {
                                            viewModel.setPlayMode(PatternPlayMode.PLAYLIST)
                                            viewModel.setVibrationEnabled(true)
                                        },
                                        onLongPress = { viewModel.removeFromPlaylistAt(index) }
                                    )
                                }
                            }
                        }
                    }
                    items(savedPlaylists, key = { "saved-${it.name}" }) { playlist ->
                        FigmaPlaylistCard(
                            title = playlist.name,
                            subtitle = strings.playlist,
                            expanded = expandedPlaylist == playlist.name,
                            onHeaderClick = {
                                expandedPlaylist = if (expandedPlaylist == playlist.name) null else playlist.name
                                viewModel.loadPlaylist(playlist.name)
                            },
                            onHeaderLongPress = { viewModel.deleteSavedPlaylist(playlist.name) }
                        ) {
                            playlist.items.forEach { item ->
                                FigmaNestedSelectableItem(
                                    emoji = "💗",
                                    title = patternLabel(item.key, language),
                                    subtitle = strings.playlist,
                                    onClick = { viewModel.loadPlaylist(playlist.name) }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (tab == 2) {
            FigmaCircleButton(
                onClick = { showSavePlaylistDialog = true },
                size = 60.dp,
                fill = FigmaFill.Coral,
                enabled = playlistQueue.isNotEmpty(),
                modifier = Modifier.offset(x = 300.dp, y = 627.dp)
            ) {
                Image(
                    painter = painterResource(Res.drawable.fig_ic_plus),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    colorFilter = ColorFilter.tint(androidx.compose.ui.graphics.Color.White)
                )
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
}

@Composable
private fun FigmaCustomTab(
    isRecording: Boolean,
    hint: String,
    startLabel: String,
    finishLabel: String,
    onToggle: () -> Unit,
    onSample: (Float, Float) -> Unit
) {
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(30.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(444.dp)
                .clip(RoundedCornerShape(FigmaTokens.Radius.r38))
                .then(
                    if (isRecording) {
                        Modifier.pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    onSample(
                                        (offset.x / size.width).coerceIn(0f, 1f),
                                        (1f - offset.y / size.height).coerceIn(0f, 1f)
                                    )
                                },
                                onDrag = { change, _ ->
                                    change.consume()
                                    onSample(
                                        (change.position.x / size.width).coerceIn(0f, 1f),
                                        (1f - change.position.y / size.height).coerceIn(0f, 1f)
                                    )
                                }
                            )
                        }
                    } else Modifier
                ),
            contentAlignment = Alignment.BottomCenter
        ) {
            Image(
                painter = painterResource(Res.drawable.fig_touch_area),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.fillMaxSize()
            )
            Text(
                text = hint,
                color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.5f),
                style = figmaGilroy(size = 14),
                modifier = Modifier.padding(bottom = 20.dp)
            )
        }
        FigmaPillButton(
            onClick = onToggle,
            fill = if (isRecording) FigmaFill.Coral else FigmaFill.Dark,
            modifier = Modifier.fillMaxWidth().height(60.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Image(
                    painter = painterResource(Res.drawable.fig_ic_rec),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    colorFilter = ColorFilter.tint(
                        if (isRecording) androidx.compose.ui.graphics.Color.White else FigmaCoralStart
                    )
                )
                Text(
                    text = if (isRecording) finishLabel else startLabel,
                    color = androidx.compose.ui.graphics.Color.White,
                    style = figmaGilroy(size = 16)
                )
            }
        }
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
