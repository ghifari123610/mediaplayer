package com.example.domain.model

data class TrackItem(
    val id: Int = 0,
    val title: String,
    val artist: String,
    val album: String,
    val durationText: String,
    val durationSeconds: Int,
    val isVideo: Boolean,
    val mediaUrl: String,
    val iconGradientStart: String,
    val iconGradientEnd: String
)
