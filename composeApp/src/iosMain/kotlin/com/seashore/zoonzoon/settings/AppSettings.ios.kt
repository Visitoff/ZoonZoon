package com.seashore.zoonzoon.settings

import com.seashore.zoonzoon.i18n.AppLanguage
import platform.Foundation.NSUserDefaults

actual fun createAppSettings(): AppSettings = IosAppSettings()

private class IosAppSettings : AppSettings {
    private val defaults = NSUserDefaults.standardUserDefaults

    override fun getVibrationTarget(): VibrationTarget {
        val name = defaults.stringForKey(KEY_TARGET) ?: return VibrationTarget.GAMEPAD_ONLY
        return runCatching { VibrationTarget.valueOf(name) }
            .getOrDefault(VibrationTarget.GAMEPAD_ONLY)
    }

    override fun setVibrationTarget(target: VibrationTarget) {
        defaults.setObject(target.name, KEY_TARGET)
    }

    override fun hasChosenVibrationTarget(): Boolean =
        defaults.boolForKey(KEY_CHOSEN)

    override fun setHasChosenVibrationTarget(value: Boolean) {
        defaults.setBool(value, KEY_CHOSEN)
    }

    override fun getIntensity(): Float {
        if (defaults.objectForKey(KEY_INTENSITY) == null) return DEFAULT_INTENSITY
        return defaults.floatForKey(KEY_INTENSITY).coerceIn(0f, 1f)
    }

    override fun setIntensity(value: Float) {
        defaults.setFloat(value.coerceIn(0f, 1f), KEY_INTENSITY)
    }

    override fun getSharpness(): Float {
        if (defaults.objectForKey(KEY_SHARPNESS) == null) return DEFAULT_SHARPNESS
        return defaults.floatForKey(KEY_SHARPNESS).coerceIn(0f, 1f)
    }

    override fun setSharpness(value: Float) {
        defaults.setFloat(value.coerceIn(0f, 1f), KEY_SHARPNESS)
    }

    override fun getLanguage(): AppLanguage {
        val tag = defaults.stringForKey(KEY_LANGUAGE) ?: return AppLanguage.EN_US
        return AppLanguage.entries.find { it.tag == tag } ?: AppLanguage.EN_US
    }

    override fun setLanguage(language: AppLanguage) {
        defaults.setObject(language.tag, KEY_LANGUAGE)
    }

    override fun getThemeMode(): AppThemeMode {
        val name = defaults.stringForKey(KEY_THEME) ?: return AppThemeMode.SYSTEM
        return runCatching { AppThemeMode.valueOf(name) }.getOrDefault(AppThemeMode.SYSTEM)
    }

    override fun setThemeMode(mode: AppThemeMode) {
        defaults.setObject(mode.name, KEY_THEME)
    }

    override fun getCustomPatternsData(): String = defaults.stringForKey(KEY_CUSTOM) ?: ""
    override fun setCustomPatternsData(data: String) {
        defaults.setObject(data, KEY_CUSTOM)
    }

    override fun getPlaylistQueueData(): String = defaults.stringForKey(KEY_QUEUE) ?: ""
    override fun setPlaylistQueueData(data: String) {
        defaults.setObject(data, KEY_QUEUE)
    }

    override fun getSavedPlaylistsData(): String = defaults.stringForKey(KEY_PLAYLISTS) ?: ""
    override fun setSavedPlaylistsData(data: String) {
        defaults.setObject(data, KEY_PLAYLISTS)
    }

    override fun getPatternPlayMode(): String = defaults.stringForKey(KEY_PLAY_MODE) ?: "single"
    override fun setPatternPlayMode(mode: String) {
        defaults.setObject(mode, KEY_PLAY_MODE)
    }

    override fun getActivePresetKey(): String = defaults.stringForKey(KEY_ACTIVE_PRESET) ?: "preset:steady"
    override fun setActivePresetKey(key: String) {
        defaults.setObject(key, KEY_ACTIVE_PRESET)
    }

    companion object {
        private const val KEY_TARGET = "vibration_target"
        private const val KEY_CHOSEN = "vibration_target_chosen"
        private const val KEY_INTENSITY = "intensity"
        private const val KEY_SHARPNESS = "sharpness"
        private const val KEY_LANGUAGE = "language"
        private const val KEY_THEME = "theme_mode"
        private const val KEY_CUSTOM = "custom_patterns"
        private const val KEY_QUEUE = "playlist_queue"
        private const val KEY_PLAYLISTS = "saved_playlists"
        private const val KEY_PLAY_MODE = "pattern_play_mode"
        private const val KEY_ACTIVE_PRESET = "active_preset_key"
        private const val DEFAULT_INTENSITY = 0.7f
        private const val DEFAULT_SHARPNESS = 0.5f
    }
}
