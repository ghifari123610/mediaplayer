package com.example.domain.repository

import com.example.data.database.SettingsEntity
import com.example.domain.model.TrackItem
import kotlinx.coroutines.flow.Flow

interface MediaRepository {
    fun getUserSettings(): Flow<SettingsEntity>
    suspend fun updateSettings(settings: SettingsEntity)
    suspend fun getDirectSettings(): SettingsEntity?
    
    fun getPlaylist(): Flow<List<TrackItem>>
    suspend fun addTrack(track: TrackItem)
    suspend fun removeTrack(track: TrackItem)
    suspend fun populateDefaultPlaylist()
}
