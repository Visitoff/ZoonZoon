package com.seashore.zoonzoon.gamepad.viewmodel

import com.seashore.zoonzoon.gamepad.engine.VibrationEngine
import com.seashore.zoonzoon.gamepad.model.ConnectionState
import com.seashore.zoonzoon.gamepad.model.VibrationPattern
import com.seashore.zoonzoon.gamepad.model.VibrationState
import com.seashore.zoonzoon.gamepad.model.PatternManager
import com.seashore.zoonzoon.gamepad.model.DisplayError
import com.seashore.zoonzoon.gamepad.model.ErrorSeverity
import kotlinx.coroutines.CoroutineScope
import com.seashore.zoonzoon.settings.AppSettings
import com.seashore.zoonzoon.settings.MemoryAppSettings
import com.seashore.zoonzoon.settings.VibrationTarget
import com.seashore.zoonzoon.settings.AppThemeMode
import com.seashore.zoonzoon.i18n.AppLanguage
import com.seashore.zoonzoon.gamepad.model.CustomPatternEntry
import com.seashore.zoonzoon.gamepad.model.PatternPlayMode
import com.seashore.zoonzoon.gamepad.model.PresetPatternId
import com.seashore.zoonzoon.gamepad.model.PresetPatterns
import com.seashore.zoonzoon.gamepad.model.PatternReference
import com.seashore.zoonzoon.gamepad.model.PlaylistQueueItem
import com.seashore.zoonzoon.gamepad.model.SavedPlaylist
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for gamepad vibration control.
 *
 * This ViewModel manages the UI state for controller connection and vibration status.
 * It exposes [ConnectionState] and [VibrationState] as [StateFlow] objects that the
 * UI layer can observe reactively.
 *
 * The ViewModel coordinates user actions (enable/disable vibration, pattern selection,
 * intensity adjustment) by delegating to the [VibrationEngine]. It also listens to
 * state changes from the engine and updates its own state accordingly.
 *
 * **Validates: Requirements 2.1, 2.2, 3.1, 4.1, 8.1, 8.2, 8.3, 8.4, 8.5, 10.1, 10.2, 10.3, 10.4, 10.5**
 *
 * @param vibrationEngine The [VibrationEngine] instance that handles vibration execution.
 * @param scope The [CoroutineScope] for launching coroutines. In production, this would
 *              typically be viewModelScope from AndroidX ViewModel.
 */
class GamepadViewModel(
    private val vibrationEngine: VibrationEngine,
    private val scope: CoroutineScope,
    private val patternManager: PatternManager = PatternManager(),
    private val appSettings: AppSettings = MemoryAppSettings(),
    vibrationTargetFlow: MutableStateFlow<VibrationTarget>? = null
) {
    private val patternRepository = PatternRepository(appSettings)

    /**
     * Observable connection state.
     * Emits updates when controllers connect or disconnect.
     *
     * **Validates: Requirement 8.1**
     */
    val connectionState: StateFlow<ConnectionState> = vibrationEngine.controller.connectionState

    /**
     * Observable vibration state.
     * Emits updates when vibration is enabled/disabled, pattern changes, or intensity changes.
     *
     * **Validates: Requirement 8.2**
     */
    val vibrationState: StateFlow<VibrationState> = vibrationEngine.vibrationState

    // Error state management
    private val _errorState = MutableStateFlow<DisplayError?>(null)
    
    /**
     * Observable error state.
     * Emits updates when errors occur or are cleared.
     *
     * **Validates: Requirements 10.1, 10.2, 10.3, 10.4, 10.5**
     */
    val errorState: StateFlow<DisplayError?> = _errorState.asStateFlow()

    private val _vibrationTarget = vibrationTargetFlow
        ?: MutableStateFlow(appSettings.getVibrationTarget())
    val vibrationTarget: StateFlow<VibrationTarget> = _vibrationTarget.asStateFlow()

    private val _showGamepadHelp = MutableStateFlow(false)
    val showGamepadHelp: StateFlow<Boolean> = _showGamepadHelp.asStateFlow()

    private val _showVibrationTargetPrompt = MutableStateFlow(false)
    val showVibrationTargetPrompt: StateFlow<Boolean> = _showVibrationTargetPrompt.asStateFlow()

    private val _language = MutableStateFlow(appSettings.getLanguage())
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    private val _themeMode = MutableStateFlow(appSettings.getThemeMode())
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    private val _playMode = MutableStateFlow(patternRepository.loadPlayMode())
    val playMode: StateFlow<PatternPlayMode> = _playMode.asStateFlow()

    private val _activePatternKey = MutableStateFlow(appSettings.getActivePresetKey())
    val activePatternKey: StateFlow<String> = _activePatternKey.asStateFlow()

    private val _customPatterns = MutableStateFlow(patternRepository.loadCustomPatterns())
    val customPatterns: StateFlow<List<CustomPatternEntry>> = _customPatterns.asStateFlow()

    private val _playlistQueue = MutableStateFlow(patternRepository.loadQueue())
    val playlistQueue: StateFlow<List<PlaylistQueueItem>> = _playlistQueue.asStateFlow()

    private val _savedPlaylists = MutableStateFlow(patternRepository.loadSavedPlaylists())
    val savedPlaylists: StateFlow<List<SavedPlaylist>> = _savedPlaylists.asStateFlow()

    private val _recorderState = MutableStateFlow(RecorderUiState())
    val recorderState: StateFlow<RecorderUiState> = _recorderState.asStateFlow()

    init {
        observeEngineStateChanges()
        vibrationEngine.setIntensity(appSettings.getIntensity())
        vibrationEngine.setSharpness(appSettings.getSharpness())
        applyActivePatternToEngine()
    }

    /** Call once when the app shell is shown (not from unit tests). */
    fun onAppStarted() {
        observeConnectionForTargetPrompt()
        startDiscovery()
    }

    /**
     * Enable or disable vibration.
     *
     * When enabled, the currently selected pattern will start executing.
     * When disabled, vibration stops and a stop command (motor values 0) is sent.
     *
     * **Validates: Requirements 2.1, 2.2**
     *
     * @param enabled True to enable vibration, false to disable.
     */
    fun setVibrationEnabled(enabled: Boolean) {
        if (enabled) {
            vibrationEngine.enableVibration()
        } else {
            vibrationEngine.disableVibration()
        }
    }

    fun toggleVibration() {
        val enabled = vibrationState.value.enabled
        if (!enabled) applyActivePatternToEngine()
        setVibrationEnabled(!enabled)
    }

    fun openGamepadHelp() {
        _showGamepadHelp.value = true
    }

    fun dismissGamepadHelp() {
        _showGamepadHelp.value = false
    }

    fun setVibrationTarget(target: VibrationTarget) {
        _vibrationTarget.value = target
        appSettings.setVibrationTarget(target)
        appSettings.setHasChosenVibrationTarget(true)
        _showVibrationTargetPrompt.value = false
    }

    fun dismissVibrationTargetPrompt() {
        _showVibrationTargetPrompt.value = false
        appSettings.setHasChosenVibrationTarget(true)
    }

    fun maybePromptVibrationTarget() {
        val connected = connectionState.value is ConnectionState.Connected
        if (connected && !appSettings.hasChosenVibrationTarget()) {
            _showVibrationTargetPrompt.value = true
        }
    }

    val isGamepadConnected: Boolean
        get() = connectionState.value is ConnectionState.Connected

    /**
     * Set the active vibration pattern.
     *
     * The new pattern takes effect immediately. If vibration is currently enabled,
     * the new pattern will start executing on the next frame.
     *
     * **Validates: Requirement 3.1**
     *
     * @param pattern The [VibrationPattern] to activate.
     */
    fun setPattern(pattern: VibrationPattern) {
        vibrationEngine.setPattern(pattern)
    }

    /**
     * Set the vibration intensity.
     *
     * The new intensity is applied to all subsequent vibration frames. If vibration
     * is currently active, the change takes effect on the next frame.
     *
     * **Validates: Requirement 4.1**
     *
     * @param intensity Intensity in the range [0.0, 1.0].
     * @throws IllegalArgumentException if [intensity] is outside [0.0, 1.0].
     */
    fun setIntensity(intensity: Float) {
        require(intensity in 0.0f..1.0f) {
            "Intensity must be in range [0.0, 1.0], got $intensity"
        }
        vibrationEngine.setIntensity(intensity)
        appSettings.setIntensity(intensity)
    }

    fun setSharpness(sharpness: Float) {
        require(sharpness in 0.0f..1.0f) {
            "Sharpness must be in range [0.0, 1.0], got $sharpness"
        }
        vibrationEngine.setSharpness(sharpness)
        appSettings.setSharpness(sharpness)
    }

    fun setLanguage(language: AppLanguage) {
        _language.value = language
        appSettings.setLanguage(language)
    }

    fun setThemeMode(mode: AppThemeMode) {
        _themeMode.value = mode
        appSettings.setThemeMode(mode)
    }

    fun selectPreset(id: PresetPatternId) {
        _playMode.value = PatternPlayMode.SINGLE
        patternRepository.savePlayMode(PatternPlayMode.SINGLE)
        _activePatternKey.value = PatternReference.Preset(id).key
        appSettings.setActivePresetKey(_activePatternKey.value)
        vibrationEngine.setPattern(PresetPatterns.patternFor(id))
    }

    fun selectCustomPattern(name: String) {
        val entry = _customPatterns.value.find { it.name == name } ?: return
        _playMode.value = PatternPlayMode.SINGLE
        patternRepository.savePlayMode(PatternPlayMode.SINGLE)
        _activePatternKey.value = "custom:$name"
        appSettings.setActivePresetKey(_activePatternKey.value)
        vibrationEngine.setPattern(entry.pattern)
    }

    fun setPlayMode(mode: PatternPlayMode) {
        _playMode.value = mode
        patternRepository.savePlayMode(mode)
        applyActivePatternToEngine()
    }

    fun addToPlaylist(key: String) {
        val updated = _playlistQueue.value + PlaylistQueueItem(key)
        _playlistQueue.value = updated
        patternRepository.saveQueue(updated)
    }

    fun removeFromPlaylistAt(index: Int) {
        val list = _playlistQueue.value.toMutableList()
        if (index !in list.indices) return
        list.removeAt(index)
        _playlistQueue.value = list
        patternRepository.saveQueue(list)
    }

    fun setPlaylistItemDuration(index: Int, durationMs: Long) {
        val list = _playlistQueue.value.toMutableList()
        if (index !in list.indices) return
        val item = list[index]
        list[index] = item.copy(
            durationMs = durationMs.coerceIn(
                PlaylistQueueItem.MIN_DURATION_MS,
                PlaylistQueueItem.MAX_DURATION_MS
            )
        )
        _playlistQueue.value = list
        patternRepository.saveQueue(list)
        if (_playMode.value == PatternPlayMode.PLAYLIST) applyActivePatternToEngine()
    }

    fun movePlaylistItem(index: Int, delta: Int) {
        val list = _playlistQueue.value.toMutableList()
        val target = index + delta
        if (index !in list.indices || target !in list.indices) return
        val item = list.removeAt(index)
        list.add(target, item)
        _playlistQueue.value = list
        patternRepository.saveQueue(list)
    }

    fun clearPlaylist() {
        _playlistQueue.value = emptyList()
        patternRepository.saveQueue(emptyList())
    }

    fun deleteCustomPattern(name: String) {
        val customKey = PatternReference.Custom(name).key
        val updated = _customPatterns.value.filterNot { it.name == name }
        _customPatterns.value = updated
        patternRepository.saveCustomPatterns(updated)

        val queue = _playlistQueue.value.filterNot { it.key == customKey }
        if (queue.size != _playlistQueue.value.size) {
            _playlistQueue.value = queue
            patternRepository.saveQueue(queue)
        }

        val playlists = _savedPlaylists.value.map { playlist ->
            playlist.copy(items = playlist.items.filterNot { it.key == customKey })
        }.filter { it.items.isNotEmpty() }
        _savedPlaylists.value = playlists
        patternRepository.saveSavedPlaylists(playlists)

        if (_activePatternKey.value == customKey) {
            selectPreset(PresetPatternId.STEADY)
        }
        if (_playMode.value == PatternPlayMode.PLAYLIST) applyActivePatternToEngine()
    }

    fun deleteSavedPlaylist(name: String) {
        val updated = _savedPlaylists.value.filterNot { it.name == name }
        _savedPlaylists.value = updated
        patternRepository.saveSavedPlaylists(updated)
    }

    fun saveCurrentQueueAsPlaylist(name: String) {
        if (name.isBlank() || _playlistQueue.value.isEmpty()) return
        val updated = _savedPlaylists.value.filterNot { it.name == name } +
            SavedPlaylist(name, _playlistQueue.value)
        _savedPlaylists.value = updated
        patternRepository.saveSavedPlaylists(updated)
    }

    fun loadPlaylist(name: String) {
        val playlist = _savedPlaylists.value.find { it.name == name } ?: return
        _playlistQueue.value = playlist.items
        patternRepository.saveQueue(playlist.items)
        setPlayMode(PatternPlayMode.PLAYLIST)
    }

    fun startRecorder() {
        _recorderState.value = RecorderUiState(isRecording = true)
    }

    fun addRecorderSample(normalizedX: Float, normalizedY: Float) {
        if (!_recorderState.value.isRecording) return
        val freq = (0.5f + normalizedX.coerceIn(0f, 1f) * 7.5f)
        val intensity = normalizedY.coerceIn(0f, 1f)
        val sample = VibrationPattern.Recorded.Sample(intensity, freq)
        val updated = _recorderState.value.samples + sample
        _recorderState.value = _recorderState.value.copy(samples = updated)
        val preview = VibrationPattern.Recorded(updated)
        vibrationEngine.setPattern(preview)
        if (!vibrationState.value.enabled) vibrationEngine.enableVibration()
    }

    fun finishRecorder() {
        _recorderState.value = _recorderState.value.copy(
            isRecording = false,
            showSaveDialog = true
        )
        vibrationEngine.disableVibration()
    }

    fun dismissRecorderSaveDialog() {
        _recorderState.value = RecorderUiState()
    }

    fun saveRecordedPattern(name: String) {
        val samples = _recorderState.value.samples
        if (name.isBlank() || samples.isEmpty()) return
        val entry = CustomPatternEntry(name, VibrationPattern.Recorded(samples))
        val updated = _customPatterns.value.filterNot { it.name == name } + entry
        _customPatterns.value = updated
        patternRepository.saveCustomPatterns(updated)
        _recorderState.value = RecorderUiState()
        selectCustomPattern(name)
    }

    fun isPatternSelected(key: String): Boolean = _activePatternKey.value == key &&
        _playMode.value == PatternPlayMode.SINGLE

    private fun applyActivePatternToEngine() {
        when (_playMode.value) {
            PatternPlayMode.SINGLE -> {
                val pattern = patternRepository.resolvePattern(
                    _activePatternKey.value,
                    _customPatterns.value
                ) ?: PresetPatterns.patternFor(PresetPatternId.STEADY)
                vibrationEngine.setPattern(pattern)
            }
            PatternPlayMode.PLAYLIST -> {
                val segments = mutableListOf<VibrationPattern>()
                val durations = mutableListOf<Long>()
                _playlistQueue.value.forEach { item ->
                    patternRepository.resolvePattern(item.key, _customPatterns.value)?.let { pattern ->
                        segments.add(pattern)
                        durations.add(item.durationMs)
                    }
                }
                vibrationEngine.setPattern(
                    VibrationPattern.Sequence(
                        segments = segments,
                        segmentDurationsMs = durations
                    )
                )
            }
        }
    }

    /**
     * Save a custom pattern and optionally activate it.
     *
     * **Validates: Requirement 3.5**
     */
    fun saveAndApplyCustomPattern(
        name: String,
        frames: List<VibrationPattern.Custom.Frame>,
        loop: Boolean = true
    ) {
        val pattern = VibrationPattern.Custom(frames = frames, loop = loop)
        patternManager.removeCustomPattern(name)
        patternManager.registerCustomPattern(name, pattern)
        vibrationEngine.setPattern(pattern)
    }

    /**
     * Get all available patterns including custom ones.
     */
    fun getAllPatterns(): List<VibrationPattern> = patternManager.getAllPatterns()

    /**
     * Start controller discovery.
     * **Validates: Requirement 1.1**
     */
    fun startDiscovery() {
        scope.launch {
            try {
                vibrationEngine.controller.startDiscovery()
            } catch (e: Exception) {
                _errorState.value = DisplayError(
                    message = "Failed to start discovery: ${e.message}",
                    severity = ErrorSeverity.ERROR
                )
            }
        }
    }

    /**
     * Stop controller discovery.
     */
    fun stopDiscovery() {
        scope.launch {
            vibrationEngine.controller.stopDiscovery()
        }
    }

    /**
     * Disconnect from the current controller.
     * **Validates: Requirement 1.4**
     */
    fun disconnect() {
        scope.launch {
            vibrationEngine.disableVibration()
            vibrationEngine.controller.disconnect()
        }
    }

    /**
     * Observe state changes from the VibrationEngine.
     *
     * This method sets up observation of the engine's state. Since the engine
     * exposes its state as StateFlow, the ViewModel can directly expose these
     * flows to the UI layer. State updates propagate automatically.
     *
     * **Validates: Requirements 8.3, 8.4, 8.5**
     */
    private fun observeEngineStateChanges() {
        // The VibrationEngine already exposes StateFlow objects for both
        // connectionState (via controller) and vibrationState.
        // We expose these directly to the UI layer, so state updates
        // propagate automatically without additional observation logic.
        //
        // This design ensures state consistency between the ViewModel,
        // VibrationEngine, and UI_Layer as required by Requirement 8.5.
    }

    private fun observeConnectionForTargetPrompt() {
        scope.launch {
            connectionState.collect { state ->
                if (state is ConnectionState.Connected) {
                    maybePromptVibrationTarget()
                }
            }
        }
    }
}
