package com.example.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.data.database.AppDatabase
import com.example.data.database.SettingsEntity
import com.example.data.repository.MediaRepositoryImpl
import com.example.domain.model.TrackItem
import com.example.domain.repository.MediaRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    // Database Initialization (Manual Dependency Injection for safety)
    private val database: AppDatabase by lazy {
        Room.databaseBuilder(
            application,
            AppDatabase::class.java,
            "custom_mediaplayer.db"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    private val mediaRepository: MediaRepository by lazy {
        MediaRepositoryImpl(
            database.settingsDao(),
            database.playlistDao()
        )
    }

    // Playback State variables (simulating ExoPlayer state controller)
    var isPlaying by mutableStateOf(false)
        private set

    var currentTrackIndex by mutableStateOf(0)
        private set

    var playbackPositionMs by mutableStateOf(0L)
        private set

    var activeVolume by mutableStateOf(0.7f) // 0.0 to 1.0
        private set

    var activeBrightness by mutableStateOf(0.8f) // 0.0 to 1.0
        private set

    // Observing Room Data Reactive Flows
    val userSettings: StateFlow<SettingsEntity> = mediaRepository.getUserSettings()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SettingsEntity()
        )

    val playlist: StateFlow<List<TrackItem>> = mediaRepository.getPlaylist()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Animated Visualizer State (Emitting spectrum data)
    private val _spectrumData = MutableStateFlow(List(16) { 0.2f })
    val spectrumData: StateFlow<List<Float>> = _spectrumData.asStateFlow()

    private var playbackJob: Job? = null
    private var visualizerJob: Job? = null

    init {
        viewModelScope.launch {
            // First Launch check -- Populate default tracks
            mediaRepository.populateDefaultPlaylist()
        }
        
        // Start simulated visualizer thread
        startVisualizerLoop()
    }

    // Interactive Functions for Core Playback
    fun togglePlayPause() {
        isPlaying = !isPlaying
        if (isPlaying) {
            startPlaybackLoop()
        } else {
            playbackJob?.cancel()
        }
    }

    fun playTrack(index: Int) {
        val tracksList = playlist.value
        if (index in tracksList.indices) {
            currentTrackIndex = index
            playbackPositionMs = 0L
            if (!isPlaying) {
                isPlaying = true
                startPlaybackLoop()
            }
        }
    }

    fun nextTrack() {
        val count = playlist.value.size
        if (count > 0) {
            playTrack((currentTrackIndex + 1) % count)
        }
    }

    fun previousTrack() {
        val count = playlist.value.size
        if (count > 0) {
            val prev = if (currentTrackIndex - 1 < 0) count - 1 else currentTrackIndex - 1
            playTrack(prev)
        }
    }

    fun seekToMs(position: Long) {
        val currentTrack = currentTrack() ?: return
        val maxMs = currentTrack.durationSeconds * 1000L
        playbackPositionMs = position.coerceIn(0L, maxMs)
    }

    fun currentTrack(): TrackItem? {
        val tracks = playlist.value
        return if (tracks.isNotEmpty() && currentTrackIndex in tracks.indices) {
            tracks[currentTrackIndex]
        } else null
    }

    // Dynamic Database Settable State modifiers
    fun updateThemePreset(preset: String) {
        viewModelScope.launch {
            val settings = userSettings.value
            mediaRepository.updateSettings(settings.copy(themePreset = preset, useDynamicColor = false))
        }
    }

    fun toggleDynamicColor(enabled: Boolean) {
        viewModelScope.launch {
            val settings = userSettings.value
            mediaRepository.updateSettings(settings.copy(useDynamicColor = enabled, themePreset = if (enabled) "custom" else "dark_cosmic"))
        }
    }

    fun updateCustomColors(primary: String, secondary: String, background: String, surface: String) {
        viewModelScope.launch {
            val settings = userSettings.value
            mediaRepository.updateSettings(
                settings.copy(
                    themePreset = "custom",
                    useDynamicColor = false,
                    customPrimaryColor = primary,
                    customSecondaryColor = secondary,
                    customBackgroundColor = background,
                    customSurfaceColor = surface
                )
            )
        }
    }

    fun updateCoverArtSize(size: Float) {
        viewModelScope.launch {
            val settings = userSettings.value
            mediaRepository.updateSettings(settings.copy(coverArtSize = size))
        }
    }

    fun setGridView(enabled: Boolean) {
        viewModelScope.launch {
            val settings = userSettings.value
            mediaRepository.updateSettings(settings.copy(isGridView = enabled))
        }
    }

    // EQ Adjustments (Stored instantly in Local SQLite Room)
    fun updateEqualizer(bandIndex: Int, value: Float) {
        viewModelScope.launch {
            val s = userSettings.value
            val updated = when (bandIndex) {
                0 -> s.copy(eq60Hz = value)
                1 -> s.copy(eq230Hz = value)
                2 -> s.copy(eq910Hz = value)
                3 -> s.copy(eq4kHz = value)
                4 -> s.copy(eq14kHz = value)
                else -> s
            }
            mediaRepository.updateSettings(updated)
        }
    }

    fun updateBassBoost(value: Float) {
        viewModelScope.launch {
            val s = userSettings.value
            mediaRepository.updateSettings(s.copy(bassBoost = value))
        }
    }

    // Gestures Adjustment
    fun toggleGestureSeek(enabled: Boolean) {
        viewModelScope.launch {
             val s = userSettings.value
             mediaRepository.updateSettings(s.copy(gestureSeekEnabled = enabled))
        }
    }

    fun toggleGestureVolume(enabled: Boolean) {
        viewModelScope.launch {
             val s = userSettings.value
             mediaRepository.updateSettings(s.copy(gestureVolumeEnabled = enabled))
        }
    }

    fun toggleGestureBrightness(enabled: Boolean) {
        viewModelScope.launch {
             val s = userSettings.value
             mediaRepository.updateSettings(s.copy(gestureBrightnessEnabled = enabled))
        }
    }

    // Gestures Event listeners from Area swipe actions
    fun adjustVolumeBySwipe(delta: Float) {
        activeVolume = (activeVolume + delta).coerceIn(0.0f, 1.0f)
    }

    fun adjustBrightnessBySwipe(delta: Float) {
        activeBrightness = (activeBrightness + delta).coerceIn(0.0f, 1.0f)
    }

    fun performSeekSwipe(deltaMs: Long) {
        val currentTrack = currentTrack() ?: return
        val maxMs = currentTrack.durationSeconds * 1000L
        playbackPositionMs = (playbackPositionMs + deltaMs).coerceIn(0L, maxMs)
    }

    // Loops & Helpers
    private fun startPlaybackLoop() {
        playbackJob?.cancel()
        playbackJob = viewModelScope.launch {
            while (isPlaying) {
                delay(1000)
                val current = currentTrack() ?: break
                val maxMs = current.durationSeconds * 1000L
                if (playbackPositionMs < maxMs) {
                    playbackPositionMs += 1000
                } else {
                    // Skip to next track
                    nextTrack()
                }
            }
        }
    }

    private fun startVisualizerLoop() {
        visualizerJob?.cancel()
        visualizerJob = viewModelScope.launch {
            while (true) {
                delay(120)
                if (isPlaying) {
                    // Make wave dance based on EQ & Bass boost parameters
                    val settings = userSettings.value
                    val bassMultiplier = 1.0f + (settings.bassBoost / 10.0f) * 1.5f
                    val eqFactor = (settings.eq60Hz + settings.eq230Hz + settings.eq910Hz + settings.eq4kHz + settings.eq14kHz) / 50.0f + 1.0f
                    
                    _spectrumData.value = List(16) { index ->
                        // Base height
                        var height = 0.15f + kotlin.random.Random.nextFloat() * (0.85f - 0.15f)
                        // Apply bass to early bins (low frequency)
                        if (index < 5) {
                            height *= bassMultiplier
                        }
                        // Apply equalizer offset
                        height *= eqFactor
                        height.coerceIn(0.05f, 1.0f)
                    }
                } else {
                    // Static resting wave
                    _spectrumData.value = List(16) { index ->
                        0.1f + 0.05f * (index % 3)
                    }
                }
            }
        }
    }

    override fun onCleared() {
        playbackJob?.cancel()
        visualizerJob?.cancel()
        super.onCleared()
    }
}
