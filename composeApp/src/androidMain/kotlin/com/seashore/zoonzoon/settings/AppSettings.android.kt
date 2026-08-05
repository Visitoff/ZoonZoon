package com.seashore.zoonzoon.settings

import android.content.Context
import com.seashore.zoonzoon.i18n.AppLanguage

private lateinit var settingsContext: Context

internal fun initAppSettingsContext(context: Context) {
    settingsContext = context.applicationContext
}

actual fun createAppSettings(): AppSettings = AndroidAppSettings()

private class AndroidAppSettings : AppSettings {
    private val prefs by lazy {
        settingsContext.getSharedPreferences("zoonzoon_prefs", Context.MODE_PRIVATE)
    }

    override fun getVibrationTarget(): VibrationTarget {
        val name = prefs.getString(KEY_TARGET, VibrationTarget.GAMEPAD_ONLY.name)
        return runCatching { VibrationTarget.valueOf(name!!) }
            .getOrDefault(VibrationTarget.GAMEPAD_ONLY)
    }

    override fun setVibrationTarget(target: VibrationTarget) {
        prefs.edit().putString(KEY_TARGET, target.name).apply()
    }

    override fun hasChosenVibrationTarget(): Boolean =
        prefs.getBoolean(KEY_CHOSEN, false)

    override fun setHasChosenVibrationTarget(value: Boolean) {
        prefs.edit().putBoolean(KEY_CHOSEN, value).apply()
    }

    override fun getIntensity(): Float =
        prefs.getFloat(KEY_INTENSITY, DEFAULT_INTENSITY).coerceIn(0f, 1f)

    override fun setIntensity(value: Float) {
        prefs.edit().putFloat(KEY_INTENSITY, value.coerceIn(0f, 1f)).apply()
    }

    override fun getSharpness(): Float =
        prefs.getFloat(KEY_SHARPNESS, DEFAULT_SHARPNESS).coerceIn(0f, 1f)

    override fun setSharpness(value: Float) {
        prefs.edit().putFloat(KEY_SHARPNESS, value.coerceIn(0f, 1f)).apply()
    }

    override fun getLanguage(): AppLanguage {
        val tag = prefs.getString(KEY_LANGUAGE, AppLanguage.EN_US.tag)
        return AppLanguage.entries.find { it.tag == tag } ?: AppLanguage.EN_US
    }

    override fun setLanguage(language: AppLanguage) {
        prefs.edit().putString(KEY_LANGUAGE, language.tag).apply()
    }

    override fun getThemeMode(): AppThemeMode {
        val name = prefs.getString(KEY_THEME, AppThemeMode.SYSTEM.name)
        return runCatching { AppThemeMode.valueOf(name!!) }.getOrDefault(AppThemeMode.SYSTEM)
    }

    override fun setThemeMode(mode: AppThemeMode) {
        prefs.edit().putString(KEY_THEME, mode.name).apply()
    }

    override fun getCustomPatternsData(): String = prefs.getString(KEY_CUSTOM, "") ?: ""
    override fun setCustomPatternsData(data: String) {
        prefs.edit().putString(KEY_CUSTOM, data).apply()
    }

    override fun getPlaylistQueueData(): String = prefs.getString(KEY_QUEUE, "") ?: ""
    override fun setPlaylistQueueData(data: String) {
        prefs.edit().putString(KEY_QUEUE, data).apply()
    }

    override fun getSavedPlaylistsData(): String = prefs.getString(KEY_PLAYLISTS, "") ?: ""
    override fun setSavedPlaylistsData(data: String) {
        prefs.edit().putString(KEY_PLAYLISTS, data).apply()
    }

    override fun getPatternPlayMode(): String = prefs.getString(KEY_PLAY_MODE, "single") ?: "single"
    override fun setPatternPlayMode(mode: String) {
        prefs.edit().putString(KEY_PLAY_MODE, mode).apply()
    }

    override fun getActivePresetKey(): String = prefs.getString(KEY_ACTIVE_PRESET, "preset:steady") ?: "preset:steady"
    override fun setActivePresetKey(key: String) {
        prefs.edit().putString(KEY_ACTIVE_PRESET, key).apply()
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
