package com.seashore.zoonzoon.gamepad.model

import com.seashore.zoonzoon.i18n.AppLanguage

enum class PresetPatternId(val key: String) {
    STEADY("steady"),
    PULSE("pulse"),
    WAVE("wave"),
    VOLCANO("volcano"),
    HEARTBEAT("heartbeat"),
    EARTHQUAKE("earthquake"),
    RIPPLE("ripple"),
    STORM("storm"),
    BREEZE("breeze"),
    HAMMER("hammer"),
    CASCADE("cascade"),
    SPARK("spark");

    companion object {
        fun fromKey(key: String): PresetPatternId? = entries.find { it.key == key }
    }
}

data class PresetDefinition(
    val id: PresetPatternId,
    val emoji: String,
    val nameEn: String,
    val nameJa: String,
    val descriptionEn: String,
    val pattern: VibrationPattern
)

object PresetPatterns {
    val all: List<PresetDefinition> = listOf(
        PresetDefinition(PresetPatternId.STEADY, "▬", "Steady", "一定", "Constant vibration", VibrationPattern.Constant),
        PresetDefinition(PresetPatternId.PULSE, "◉", "Pulse", "パルス", "Rhythmic on/off pulses", VibrationPattern.Pulse(2f)),
        PresetDefinition(PresetPatternId.WAVE, "〜", "Wave", "ウェーブ", "Smooth sine wave", VibrationPattern.Wave(1f)),
        PresetDefinition(PresetPatternId.VOLCANO, "🌋", "Volcano", "火山", "Build → peak → fade", VibrationPattern.Volcano),
        PresetDefinition(PresetPatternId.HEARTBEAT, "💓", "Heartbeat", "心拍", "Double beat, pause", VibrationPattern.Heartbeat),
        PresetDefinition(PresetPatternId.EARTHQUAKE, "🫨", "Earthquake", "地震", "Chaotic rumble bursts", VibrationPattern.Earthquake),
        PresetDefinition(PresetPatternId.RIPPLE, "〰️", "Ripple", "波紋", "Decaying ring waves", VibrationPattern.Ripple),
        PresetDefinition(PresetPatternId.STORM, "⛈️", "Storm", "嵐", "Fast bursts with gaps", VibrationPattern.Storm),
        PresetDefinition(PresetPatternId.BREEZE, "🍃", "Breeze", "そよ風", "Light frequent flutter", VibrationPattern.Breeze),
        PresetDefinition(PresetPatternId.HAMMER, "🔨", "Hammer", "ハンマー", "Rare strong strikes", VibrationPattern.Hammer),
        PresetDefinition(PresetPatternId.CASCADE, "💧", "Cascade", "滝", "Stepped intensity down", VibrationPattern.Cascade),
        PresetDefinition(PresetPatternId.SPARK, "✨", "Spark", "スパーク", "Short bright flashes", VibrationPattern.Spark)
    )

    fun patternFor(id: PresetPatternId): VibrationPattern =
        all.first { it.id == id }.pattern

    fun definitionFor(id: PresetPatternId): PresetDefinition =
        all.first { it.id == id }

    fun labelForKey(key: String, language: AppLanguage = AppLanguage.EN_US): String {
        return when (val ref = PatternReference.fromKey(key)) {
            is PatternReference.Preset -> {
                val def = definitionFor(ref.id)
                if (language == AppLanguage.JA) def.nameJa else def.nameEn
            }
            is PatternReference.Custom -> ref.name
            null -> key
        }
    }

    fun matchPreset(pattern: VibrationPattern): PresetPatternId? = when (pattern) {
        is VibrationPattern.Constant -> PresetPatternId.STEADY
        is VibrationPattern.Pulse -> if (pattern.frequencyHz == 2f) PresetPatternId.PULSE else null
        is VibrationPattern.Wave -> if (pattern.frequencyHz == 1f) PresetPatternId.WAVE else null
        is VibrationPattern.Volcano -> PresetPatternId.VOLCANO
        is VibrationPattern.Heartbeat -> PresetPatternId.HEARTBEAT
        is VibrationPattern.Earthquake -> PresetPatternId.EARTHQUAKE
        is VibrationPattern.Ripple -> PresetPatternId.RIPPLE
        is VibrationPattern.Storm -> PresetPatternId.STORM
        is VibrationPattern.Breeze -> PresetPatternId.BREEZE
        is VibrationPattern.Hammer -> PresetPatternId.HAMMER
        is VibrationPattern.Cascade -> PresetPatternId.CASCADE
        is VibrationPattern.Spark -> PresetPatternId.SPARK
        else -> null
    }
}

sealed class PatternReference {
    abstract val key: String

    data class Preset(val id: PresetPatternId) : PatternReference() {
        override val key: String = "preset:${id.key}"
    }

    data class Custom(val name: String) : PatternReference() {
        override val key: String = "custom:$name"
    }

    companion object {
        fun fromKey(key: String): PatternReference? = when {
            key.startsWith("preset:") -> PresetPatternId.fromKey(key.removePrefix("preset:"))?.let { Preset(it) }
            key.startsWith("custom:") -> Custom(key.removePrefix("custom:"))
            else -> null
        }
    }
}

enum class PatternPlayMode {
    SINGLE,
    PLAYLIST
}

data class SavedPlaylist(
    val name: String,
    val items: List<PlaylistQueueItem>
)

data class PlaylistQueueItem(
    val key: String,
    val durationMs: Long = DEFAULT_PLAYLIST_SEGMENT_MS
) {
    companion object {
        const val DEFAULT_PLAYLIST_SEGMENT_MS = 4_000L
        const val MIN_DURATION_MS = 1_000L
        const val MAX_DURATION_MS = 60_000L
    }
}

data class CustomPatternEntry(
    val name: String,
    val pattern: VibrationPattern.Recorded
)
