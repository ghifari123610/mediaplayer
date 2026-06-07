package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_settings")
data class SettingsEntity(
    @PrimaryKey val id: Int = 1,
    val useDynamicColor: Boolean = false,
    val themePreset: String = "elegant_dark", // elegant_dark, dark_cosmic, warm_amber, emerald_oasis, minimal_slate, custom
    val customPrimaryColor: String = "#8A2BE2", // Indigo / purple
    val customSecondaryColor: String = "#00FFFF", // Cyan
    val customBackgroundColor: String = "#121212", // Dark
    val customSurfaceColor: String = "#1E1E1E", // Dark card
    val coverArtSize: Float = 220f, // 150f to 300f
    val isGridView: Boolean = false,
    val eq60Hz: Float = 0f, // -10 to +10 dB
    val eq230Hz: Float = 0f,
    val eq910Hz: Float = 0f,
    val eq4kHz: Float = 0f,
    val eq14kHz: Float = 0f,
    val bassBoost: Float = 0f, // 0 to 10
    val gestureSeekEnabled: Boolean = true,
    val gestureVolumeEnabled: Boolean = true,
    val gestureBrightnessEnabled: Boolean = true
)
