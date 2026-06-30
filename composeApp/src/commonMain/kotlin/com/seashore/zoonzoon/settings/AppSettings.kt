package com.seashore.zoonzoon.settings

import com.seashore.zoonzoon.i18n.AppLanguage

interface AppSettings {
    fun getVibrationTarget(): VibrationTarget
    fun setVibrationTarget(target: VibrationTarget)
    fun hasChosenVibrationTarget(): Boolean
    fun setHasChosenVibrationTarget(value: Boolean)
    fun getIntensity(): Float
    fun setIntensity(value: Float)
    fun getLanguage(): AppLanguage
    fun setLanguage(language: AppLanguage)
    fun getThemeMode(): AppThemeMode
    fun setThemeMode(mode: AppThemeMode)
    fun getCustomPatternsData(): String
    fun setCustomPatternsData(data: String)
    fun getPlaylistQueueData(): String
    fun setPlaylistQueueData(data: String)
    fun getSavedPlaylistsData(): String
    fun setSavedPlaylistsData(data: String)
    fun getPatternPlayMode(): String
    fun setPatternPlayMode(mode: String)
    fun getActivePresetKey(): String
    fun setActivePresetKey(key: String)
}

/** In-memory settings for previews and unit tests. */
class MemoryAppSettings(
    initialTarget: VibrationTarget = VibrationTarget.GAMEPAD_ONLY,
    initialHasChosen: Boolean = false,
    initialIntensity: Float = 0.7f,
    initialLanguage: AppLanguage = AppLanguage.EN_US,
    initialTheme: AppThemeMode = AppThemeMode.SYSTEM
) : AppSettings {
    private var target = initialTarget
    private var hasChosen = initialHasChosen
    private var intensity = initialIntensity
    private var language = initialLanguage
    private var theme = initialTheme
    private var customPatterns = ""
    private var playlistQueue = ""
    private var savedPlaylists = ""
    private var playMode = "single"
    private var activePreset = "preset:steady"

    override fun getVibrationTarget(): VibrationTarget = target
    override fun setVibrationTarget(target: VibrationTarget) { this.target = target }
    override fun hasChosenVibrationTarget(): Boolean = hasChosen
    override fun setHasChosenVibrationTarget(value: Boolean) { hasChosen = value }
    override fun getIntensity(): Float = intensity
    override fun setIntensity(value: Float) { intensity = value.coerceIn(0f, 1f) }
    override fun getLanguage(): AppLanguage = language
    override fun setLanguage(language: AppLanguage) { this.language = language }
    override fun getThemeMode(): AppThemeMode = theme
    override fun setThemeMode(mode: AppThemeMode) { theme = mode }
    override fun getCustomPatternsData(): String = customPatterns
    override fun setCustomPatternsData(data: String) { customPatterns = data }
    override fun getPlaylistQueueData(): String = playlistQueue
    override fun setPlaylistQueueData(data: String) { playlistQueue = data }
    override fun getSavedPlaylistsData(): String = savedPlaylists
    override fun setSavedPlaylistsData(data: String) { savedPlaylists = data }
    override fun getPatternPlayMode(): String = playMode
    override fun setPatternPlayMode(mode: String) { playMode = mode }
    override fun getActivePresetKey(): String = activePreset
    override fun setActivePresetKey(key: String) { activePreset = key }
}

expect fun createAppSettings(): AppSettings
