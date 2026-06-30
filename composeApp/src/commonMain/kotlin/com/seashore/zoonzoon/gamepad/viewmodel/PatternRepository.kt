package com.seashore.zoonzoon.gamepad.viewmodel

import com.seashore.zoonzoon.gamepad.model.CustomPatternEntry
import com.seashore.zoonzoon.gamepad.model.PatternCodec
import com.seashore.zoonzoon.gamepad.model.PatternPlayMode
import com.seashore.zoonzoon.gamepad.model.PatternReference
import com.seashore.zoonzoon.gamepad.model.PlaylistQueueItem
import com.seashore.zoonzoon.gamepad.model.PresetPatternId
import com.seashore.zoonzoon.gamepad.model.PresetPatterns
import com.seashore.zoonzoon.gamepad.model.SavedPlaylist
import com.seashore.zoonzoon.gamepad.model.VibrationPattern
import com.seashore.zoonzoon.settings.AppSettings

internal class PatternRepository(private val appSettings: AppSettings) {
    fun loadCustomPatterns(): List<CustomPatternEntry> =
        PatternCodec.decodeCustomPatterns(appSettings.getCustomPatternsData())

    fun saveCustomPatterns(entries: List<CustomPatternEntry>) {
        appSettings.setCustomPatternsData(PatternCodec.encodeCustomPatterns(entries))
    }

    fun loadQueue(): List<PlaylistQueueItem> =
        PatternCodec.decodeQueue(appSettings.getPlaylistQueueData())

    fun saveQueue(items: List<PlaylistQueueItem>) {
        appSettings.setPlaylistQueueData(PatternCodec.encodeQueue(items))
    }

    fun loadSavedPlaylists(): List<SavedPlaylist> =
        PatternCodec.decodePlaylists(appSettings.getSavedPlaylistsData())

    fun saveSavedPlaylists(playlists: List<SavedPlaylist>) {
        appSettings.setSavedPlaylistsData(PatternCodec.encodePlaylists(playlists))
    }

    fun loadPlayMode(): PatternPlayMode =
        if (appSettings.getPatternPlayMode() == "playlist") PatternPlayMode.PLAYLIST else PatternPlayMode.SINGLE

    fun savePlayMode(mode: PatternPlayMode) {
        appSettings.setPatternPlayMode(if (mode == PatternPlayMode.PLAYLIST) "playlist" else "single")
    }

    fun resolvePattern(key: String, customs: List<CustomPatternEntry>): VibrationPattern? =
        when (val ref = PatternReference.fromKey(key)) {
            is PatternReference.Preset -> PresetPatterns.patternFor(ref.id)
            is PatternReference.Custom -> customs.find { it.name == ref.name }?.pattern
            null -> null
        }
}

data class RecorderUiState(
    val isRecording: Boolean = false,
    val samples: List<VibrationPattern.Recorded.Sample> = emptyList(),
    val showSaveDialog: Boolean = false
)
