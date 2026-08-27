package com.seashore.zoonzoon.ui.patterns

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.seashore.zoonzoon.gamepad.model.PatternReference
import com.seashore.zoonzoon.gamepad.model.PresetDefinition
import com.seashore.zoonzoon.gamepad.model.PresetPatternId
import com.seashore.zoonzoon.gamepad.model.PresetPatterns
import com.seashore.zoonzoon.gamepad.model.SavedPlaylist
import com.seashore.zoonzoon.gamepad.viewmodel.GamepadViewModel
import com.seashore.zoonzoon.generated.resources.Res
import com.seashore.zoonzoon.generated.resources.ic_plus
import com.seashore.zoonzoon.generated.resources.patterns_ic_rec
import com.seashore.zoonzoon.generated.resources.playlist_card_ripple
import com.seashore.zoonzoon.generated.resources.playlist_card_stripes
import com.seashore.zoonzoon.i18n.AppLanguage
import com.seashore.zoonzoon.i18n.stringsFor
import com.seashore.zoonzoon.ui.fig.FigAsset
import com.seashore.zoonzoon.ui.fig.FigCircleButton
import com.seashore.zoonzoon.ui.fig.FigColor
import com.seashore.zoonzoon.ui.fig.FigFrameWidth
import com.seashore.zoonzoon.ui.fig.figCoralRadial
import com.seashore.zoonzoon.ui.fig.figEmoji
import com.seashore.zoonzoon.ui.fig.figText
import com.seashore.zoonzoon.ui.glass.GlassTintButton
import com.seashore.zoonzoon.ui.glass.isLiquidGlassEnabled
import com.seashore.zoonzoon.ui.glass.liquidGlass

/** Figma `Screen 25` (2097:12736), 375×1238. Glass fills → Home solids. */
private val PatternsFrameHeight = 1238.dp
private val ChipIdle = FigColor.buttonSolid
private val CardFill = FigColor.black
private val PadFill = FigColor.surface
private val PillRadius = 38.dp
private val ChipRadius = 100.dp

private enum class PatternChip(val label: String) {
    All("All"),
    Rhytmic("Rhytmic"),
    Fast("Fast"),
    Pulsating("Pulsating")
}

@Composable
fun PatternsScreen(
    viewModel: GamepadViewModel,
    modifier: Modifier = Modifier
) {
    val language by viewModel.language.collectAsState()
    val recorderState by viewModel.recorderState.collectAsState()
    val savedPlaylists by viewModel.savedPlaylists.collectAsState()
    val strings = remember(language) { stringsFor(language) }
    var chip by remember { mutableStateOf(PatternChip.All) }

    val presets = remember(chip) { presetsFor(chip) }
    val playlists = remember(savedPlaylists) {
        if (savedPlaylists.isNotEmpty()) savedPlaylists
        else listOf(
            SavedPlaylist("Evening vibe", emptyList()),
            SavedPlaylist("Morning stuff", emptyList())
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Box(Modifier.size(FigFrameWidth, PatternsFrameHeight)) {
            HeaderRow(
                title = strings.patterns,
                onAdd = {},
                modifier = Modifier.offset(x = 15.dp, y = 54.dp)
            )
            ChipRow(
                selected = chip,
                onSelect = { chip = it },
                modifier = Modifier.offset(x = 18.dp, y = 124.dp)
            )
            PatternPager(
                presets = presets,
                language = language,
                isSelected = { viewModel.isPatternSelected(PatternReference.Preset(it.id).key) },
                onSelect = { viewModel.selectPreset(it.id) },
                modifier = Modifier.offset(x = 18.dp, y = 189.dp)
            )
            PlaylistSection(
                title = "Your playlists",
                playlists = playlists,
                onOpen = { viewModel.loadPlaylist(it.name) },
                modifier = Modifier.offset(x = 23.dp, y = 452.dp)
            )
            CustomSection(
                title = "Custom",
                recordLabel = if (recorderState.isRecording) strings.finishRecording else strings.startRecording,
                hint = strings.recorderHint,
                recording = recorderState.isRecording,
                onRecord = {
                    if (recorderState.isRecording) viewModel.finishRecorder()
                    else viewModel.startRecorder()
                },
                onSample = viewModel::addRecorderSample,
                modifier = Modifier.offset(x = 15.dp, y = 734.dp)
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
}

@Composable
private fun HeaderRow(title: String, onAdd: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.size(345.dp, 50.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = FigColor.white,
            style = figText(size = 30)
        )
        PlusButton(onClick = onAdd)
    }
}

@Composable
private fun PlusButton(onClick: () -> Unit) {
    FigCircleButton(
        onClick = onClick,
        diameter = 45.dp,
        fill = ChipIdle
    ) {
        FigAsset(
            resource = Res.drawable.ic_plus,
            width = 9.33333.dp,
            height = 8.66667.dp
        )
    }
}

@Composable
private fun ChipRow(
    selected: PatternChip,
    onSelect: (PatternChip) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PatternChip.entries.forEach { item ->
            val active = item == selected
            val chipShape = RoundedCornerShape(ChipRadius)
            val glass = isLiquidGlassEnabled()
            Box(
                modifier = Modifier
                    .height(45.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onSelect(item) }
                    .then(
                        when {
                            glass && active -> Modifier
                                .liquidGlass(chipShape, Color.White.copy(alpha = 0.08f))
                                .figCoralRadial(alpha = 0.45f)
                            glass -> Modifier.liquidGlass(chipShape, GlassTintButton)
                            active -> Modifier.clip(chipShape).figCoralRadial()
                            else -> Modifier.clip(chipShape).background(ChipIdle)
                        }
                    )
                    .padding(horizontal = 26.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.label,
                    color = FigColor.white.copy(alpha = if (active) 1f else 0.50f),
                    style = figText(size = 14)
                )
            }
        }
    }
}

@Composable
private fun PatternPager(
    presets: List<PresetDefinition>,
    language: AppLanguage,
    isSelected: (PresetDefinition) -> Boolean,
    onSelect: (PresetDefinition) -> Unit,
    modifier: Modifier = Modifier
) {
    val columns = presets.chunked(3)
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        columns.forEach { column ->
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                column.forEach { preset ->
                    PatternCard(
                        preset = preset,
                        language = language,
                        selected = isSelected(preset),
                        onClick = { onSelect(preset) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PatternCard(
    preset: PresetDefinition,
    language: AppLanguage,
    selected: Boolean,
    onClick: () -> Unit
) {
    val title = if (language == AppLanguage.JA) preset.nameJa else preset.nameEn
    Box(
        modifier = Modifier
            .size(278.dp, 70.dp)
            .clip(RoundedCornerShape(PillRadius))
            .background(CardFill)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        FigCircleButton(
            onClick = onClick,
            diameter = 60.dp,
            fill = Color.Transparent,
            borderColor = FigColor.signBorder,
            selected = selected,
            modifier = Modifier.offset(x = 5.dp, y = 5.dp)
        ) {
            Text(
                text = preset.emoji,
                style = figEmoji(size = 24)
            )
        }
        Text(
            text = title,
            color = FigColor.white,
            style = figText(size = 16),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.offset(x = 91.dp, y = 21.dp)
        )
        Text(
            text = preset.descriptionEn,
            color = FigColor.white.copy(alpha = 0.50f),
            style = figText(size = 14),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.offset(x = 91.dp, y = 43.dp)
        )
    }
}

@Composable
private fun PlaylistSection(
    title: String,
    playlists: List<SavedPlaylist>,
    onOpen: (SavedPlaylist) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Row(
            modifier = Modifier.size(345.dp, 50.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = FigColor.white,
                style = figText(size = 30)
            )
            PlusButton(onClick = {})
        }
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            playlists.forEachIndexed { index, playlist ->
                PlaylistCard(
                    playlist = playlist,
                    striped = index % 2 == 0,
                    onClick = { onOpen(playlist) }
                )
            }
        }
    }
}

@Composable
private fun PlaylistCard(
    playlist: SavedPlaylist,
    striped: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(220.dp, 180.dp)
            .clip(RoundedCornerShape(PillRadius))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        FigAsset(
            resource = if (striped) {
                Res.drawable.playlist_card_stripes
            } else {
                Res.drawable.playlist_card_ripple
            },
            width = 220.dp,
            height = 180.dp
        )
        Text(
            text = playlist.name,
            color = FigColor.white,
            style = figText(size = 20),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.offset(x = 20.dp, y = 30.dp)
        )
    }
}

@Composable
private fun CustomSection(
    title: String,
    recordLabel: String,
    hint: String,
    recording: Boolean,
    onRecord: () -> Unit,
    onSample: (Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.width(345.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.End
    ) {
        Row(
            modifier = Modifier.size(345.dp, 50.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = FigColor.white,
                style = figText(size = 30),
                modifier = Modifier.width(174.dp)
            )
            val recordShape = RoundedCornerShape(ChipRadius)
            val glass = isLiquidGlassEnabled()
            Row(
                modifier = Modifier
                    .size(153.dp, 45.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onRecord
                    )
                    .then(
                        when {
                            glass && recording -> Modifier
                                .liquidGlass(recordShape, Color.White.copy(alpha = 0.08f))
                                .figCoralRadial(alpha = 0.45f)
                            glass -> Modifier.liquidGlass(recordShape, GlassTintButton)
                            recording -> Modifier.clip(recordShape).figCoralRadial()
                            else -> Modifier.clip(recordShape).background(ChipIdle)
                        }
                    ),
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FigAsset(
                    resource = Res.drawable.patterns_ic_rec,
                    width = 17.dp,
                    height = 17.dp
                )
                Text(
                    text = recordLabel,
                    color = FigColor.white.copy(alpha = 0.50f),
                    style = figText(size = 14)
                )
            }
        }
        Box(
            modifier = Modifier
                .size(345.dp, 294.dp)
                .clip(RoundedCornerShape(PillRadius))
                .background(PadFill)
        ) {
            PatternRecorderPad(
                hint = hint,
                enabled = recording,
                onSample = onSample
            )
        }
    }
}

private fun presetsFor(chip: PatternChip): List<PresetDefinition> = when (chip) {
    PatternChip.All -> PresetPatterns.all
    PatternChip.Rhytmic -> PresetPatterns.all.filter {
        it.id in setOf(
            PresetPatternId.PULSE,
            PresetPatternId.HEARTBEAT,
            PresetPatternId.RIPPLE,
            PresetPatternId.WAVE
        )
    }
    PatternChip.Fast -> PresetPatterns.all.filter {
        it.id in setOf(
            PresetPatternId.STORM,
            PresetPatternId.SPARK,
            PresetPatternId.HAMMER,
            PresetPatternId.CASCADE
        )
    }
    PatternChip.Pulsating -> PresetPatterns.all.filter {
        it.id in setOf(
            PresetPatternId.STEADY,
            PresetPatternId.VOLCANO,
            PresetPatternId.EARTHQUAKE,
            PresetPatternId.BREEZE
        )
    }
}
