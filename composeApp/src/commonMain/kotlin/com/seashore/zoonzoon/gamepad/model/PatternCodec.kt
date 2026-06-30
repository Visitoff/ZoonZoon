package com.seashore.zoonzoon.gamepad.model

object PatternCodec {
    private const val ITEM_SEP = ";"
    private const val DURATION_SEP = "@"

    fun encodeRecorded(pattern: VibrationPattern.Recorded): String =
        pattern.samples.joinToString(";") { s ->
            "${s.intensity},${s.frequencyHz},${s.durationMs}"
        }

    fun decodeRecorded(data: String): VibrationPattern.Recorded? {
        if (data.isBlank()) return null
        val samples = data.split(";").mapNotNull { part ->
            val fields = part.split(",")
            if (fields.size < 3) return@mapNotNull null
            val intensity = fields[0].toFloatOrNull() ?: return@mapNotNull null
            val freq = fields[1].toFloatOrNull() ?: return@mapNotNull null
            val dur = fields[2].toLongOrNull() ?: return@mapNotNull null
            VibrationPattern.Recorded.Sample(intensity, freq, dur)
        }
        return VibrationPattern.Recorded(samples)
    }

    fun encodeCustomPatterns(entries: List<CustomPatternEntry>): String =
        entries.joinToString("\n") { entry ->
            "${entry.name}|${encodeRecorded(entry.pattern)}"
        }

    fun decodeCustomPatterns(data: String): List<CustomPatternEntry> {
        if (data.isBlank()) return emptyList()
        return data.lines().mapNotNull { line ->
            val sep = line.indexOf('|')
            if (sep <= 0) return@mapNotNull null
            val name = line.substring(0, sep)
            val pattern = decodeRecorded(line.substring(sep + 1)) ?: return@mapNotNull null
            CustomPatternEntry(name, pattern)
        }
    }

    fun encodeQueueItem(item: PlaylistQueueItem): String =
        "${item.key}$DURATION_SEP${item.durationMs}"

    fun decodeQueueItem(raw: String): PlaylistQueueItem? {
        val sep = raw.lastIndexOf(DURATION_SEP)
        if (sep <= 0) return PlaylistQueueItem(raw.trim())
        val key = raw.substring(0, sep)
        val duration = raw.substring(sep + 1).toLongOrNull()
            ?: return PlaylistQueueItem(key)
        return PlaylistQueueItem(key, duration.coerceIn(
            PlaylistQueueItem.MIN_DURATION_MS,
            PlaylistQueueItem.MAX_DURATION_MS
        ))
    }

    fun encodeQueue(items: List<PlaylistQueueItem>): String =
        items.joinToString(ITEM_SEP) { encodeQueueItem(it) }

    fun decodeQueue(data: String): List<PlaylistQueueItem> {
        if (data.isBlank()) return emptyList()
        return if (DURATION_SEP in data || ITEM_SEP in data) {
            data.split(ITEM_SEP).mapNotNull { part ->
                decodeQueueItem(part.trim())
            }
        } else {
            data.split(',').filter { it.isNotBlank() }.map { PlaylistQueueItem(it.trim()) }
        }
    }

    fun encodePlaylists(playlists: List<SavedPlaylist>): String =
        playlists.joinToString("\n") { p ->
            "${p.name}|${encodeQueue(p.items)}"
        }

    fun decodePlaylists(data: String): List<SavedPlaylist> {
        if (data.isBlank()) return emptyList()
        return data.lines().mapNotNull { line ->
            val sep = line.indexOf('|')
            if (sep <= 0) return@mapNotNull null
            val name = line.substring(0, sep)
            val items = decodeQueue(line.substring(sep + 1))
            SavedPlaylist(name, items)
        }
    }
}
