package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlist_tracks")
data class TrackEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val artist: String,
    val album: String,
    val durationText: String,
    val durationSeconds: Int,
    val isVideo: Boolean,
    val mediaUrl: String, // Stream or mock stream URL
    val iconGradientStart: String, // Color hex
    val iconGradientEnd: String // Color hex
)
