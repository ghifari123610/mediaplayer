package com.example.data.repository

import com.example.data.database.PlaylistDao
import com.example.data.database.SettingsDao
import com.example.data.database.SettingsEntity
import com.example.data.database.TrackEntity
import com.example.domain.model.TrackItem
import com.example.domain.repository.MediaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class MediaRepositoryImpl(
    private val settingsDao: SettingsDao,
    private val playlistDao: PlaylistDao
) : MediaRepository {

    override fun getUserSettings(): Flow<SettingsEntity> {
        return settingsDao.getSettingsFlow().map { it ?: SettingsEntity() }
    }

    override suspend fun updateSettings(settings: SettingsEntity) {
        settingsDao.insertOrUpdateSettings(settings)
    }

    override suspend fun getDirectSettings(): SettingsEntity? {
        return settingsDao.getSettingsDirect() ?: SettingsEntity().also {
            settingsDao.insertOrUpdateSettings(it)
        }
    }

    override fun getPlaylist(): Flow<List<TrackItem>> {
        return playlistDao.getAllTracksFlow().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun addTrack(track: TrackItem) {
        playlistDao.insertTrack(track.toEntity())
    }

    override suspend fun removeTrack(track: TrackItem) {
        playlistDao.deleteTrack(track.toEntity())
    }

    override suspend fun populateDefaultPlaylist() {
        // Only populate if list is currently empty
        val current = playlistDao.getAllTracksFlow().first()
        if (current.isEmpty()) {
            val defaults = listOf(
                TrackEntity(
                    title = "Nebula Echoes",
                    artist = "Lofi Dreamer",
                    album = "Cosmic Chill",
                    durationText = "3:45",
                    durationSeconds = 225,
                    isVideo = false,
                    mediaUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
                    iconGradientStart = "#8A2BE2",
                    iconGradientEnd = "#4B0082"
                ),
                TrackEntity(
                    title = "Cybercity Rain",
                    artist = "Neon Synth",
                    album = "Retroverse",
                    durationText = "4:12",
                    durationSeconds = 252,
                    isVideo = false,
                    mediaUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
                    iconGradientStart = "#00FFFF",
                    iconGradientEnd = "#0000FF"
                ),
                TrackEntity(
                    title = "Serenade of the Forest",
                    artist = "Acoustic Breeze",
                    album = "Organic Whispers",
                    durationText = "2:30",
                    durationSeconds = 150,
                    isVideo = false,
                    mediaUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
                    iconGradientStart = "#00FF00",
                    iconGradientEnd = "#008000"
                ),
                TrackEntity(
                    title = "Starlight Hyperdive",
                    artist = "Retro Glide",
                    album = "Vaporwave Vibe",
                    durationText = "1:55",
                    durationSeconds = 115,
                    isVideo = true,
                    mediaUrl = "https://assets.mixkit.co/videos/preview/mixkit-stars-in-space-background-1611-large.mp4",
                    iconGradientStart = "#FF4500",
                    iconGradientEnd = "#FFD700"
                ),
                TrackEntity(
                    title = "Calm Ocean Waves",
                    artist = "Nature Soundscapes",
                    album = "Deep Relaxation",
                    durationText = "5:00",
                    durationSeconds = 300,
                    isVideo = true,
                    mediaUrl = "https://assets.mixkit.co/videos/preview/mixkit-sea-water-loop-1393-large.mp4",
                    iconGradientStart = "#20B2AA",
                    iconGradientEnd = "#008B8B"
                )
            )
            playlistDao.insertAll(defaults)
        }
    }

    private fun TrackEntity.toDomain() = TrackItem(
        id = id,
        title = title,
        artist = artist,
        album = album,
        durationText = durationText,
        durationSeconds = durationSeconds,
        isVideo = isVideo,
        mediaUrl = mediaUrl,
        iconGradientStart = iconGradientStart,
        iconGradientEnd = iconGradientEnd
    )

    private fun TrackItem.toEntity() = TrackEntity(
        id = id,
        title = title,
        artist = artist,
        album = album,
        durationText = durationText,
        durationSeconds = durationSeconds,
        isVideo = isVideo,
        mediaUrl = mediaUrl,
        iconGradientStart = iconGradientStart,
        iconGradientEnd = iconGradientEnd
    )
}
