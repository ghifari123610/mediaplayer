package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Predefined Themes for the Media Player
val ElegantPrimary = Color(0xFFD0BCFF)
val ElegantSecondary = Color(0xFF381E72)
val ElegantBackground = Color(0xFF1A1C1E)
val ElegantSurface = Color(0xFF252729)

val CosmicPrimary = Color(0xFF9d4edd)
val CosmicSecondary = Color(0xFF240046)
val CosmicBackground = Color(0xFF10002b)
val CosmicSurface = Color(0xFF1c0a35)

val AmberPrimary = Color(0xFFFF9F1C)
val AmberSecondary = Color(0xFFFF4081)
val AmberBackground = Color(0xFF140D07)
val AmberSurface = Color(0xFF22160C)

val EmeraldPrimary = Color(0xFF38B000)
val EmeraldSecondary = Color(0xFF00B4D8)
val EmeraldBackground = Color(0xFF091410)
val EmeraldSurface = Color(0xFF11251E)

val SlatePrimary = Color(0xFFE2E8F0)
val SlateSecondary = Color(0xFF94A3B8)
val SlateBackground = Color(0xFF0F172A)
val SlateSurface = Color(0xFF1E293B)

@Composable
fun MediaPlayerTheme(
    themePreset: String = "elegant_dark",
    useDynamicColor: Boolean = false,
    customPrimaryHex: String = "#8A2BE2",
    customSecondaryHex: String = "#00FFFF",
    customBackgroundHex: String = "#121212",
    customSurfaceHex: String = "#1E1E1E",
    content: @Composable () -> Unit
) {
    val darkTheme = true // Keep dark theme primarily for elegant media player feel
    
    // Parse custom colors safely. Fallback to default Cosmic colors on exception.
    val customPrimary = safeParseColor(customPrimaryHex, ElegantPrimary)
    val customSecondary = safeParseColor(customSecondaryHex, ElegantSecondary)
    val customBackground = safeParseColor(customBackgroundHex, ElegantBackground)
    val customSurface = safeParseColor(customSurfaceHex, ElegantSurface)

    val colorScheme = when {
        useDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            dynamicDarkColorScheme(context)
        }
        themePreset == "elegant_dark" -> {
            darkColorScheme(
                primary = ElegantPrimary,
                secondary = ElegantSecondary,
                background = ElegantBackground,
                surface = ElegantSurface,
                onPrimary = Color(0xFF381E72),
                onSecondary = Color.White,
                onBackground = Color(0xFFE2E2E6),
                onSurface = Color(0xFFE2E2E6)
            )
        }
        themePreset == "dark_cosmic" -> {
            darkColorScheme(
                primary = CosmicPrimary,
                secondary = CosmicSecondary,
                background = CosmicBackground,
                surface = CosmicSurface,
                onPrimary = Color.White,
                onSecondary = Color.White,
                onBackground = Color(0xFFE0D7EC),
                onSurface = Color(0xFFECE6F4)
            )
        }
        themePreset == "warm_amber" -> {
            darkColorScheme(
                primary = AmberPrimary,
                secondary = AmberSecondary,
                background = AmberBackground,
                surface = AmberSurface,
                onPrimary = Color.Black,
                onSecondary = Color.White,
                onBackground = Color(0xFFFBEFE3),
                onSurface = Color(0xFFFCF5EE)
            )
        }
        themePreset == "emerald_oasis" -> {
            darkColorScheme(
                primary = EmeraldPrimary,
                secondary = EmeraldSecondary,
                background = EmeraldBackground,
                surface = EmeraldSurface,
                onPrimary = Color.Black,
                onSecondary = Color.White,
                onBackground = Color(0xFFEAF5EF),
                onSurface = Color(0xFFF3FAF7)
            )
        }
        themePreset == "minimal_slate" -> {
            darkColorScheme(
                primary = SlatePrimary,
                secondary = SlateSecondary,
                background = SlateBackground,
                surface = SlateSurface,
                onPrimary = Color.Black,
                onSecondary = Color.Black,
                onBackground = Color(0xFFF1F5F9),
                onSurface = Color(0xFFF8FAFC)
            )
        }
        themePreset == "custom" -> {
            darkColorScheme(
                primary = customPrimary,
                secondary = customSecondary,
                background = customBackground,
                surface = customSurface,
                onPrimary = if (isColorDark(customPrimary)) Color.White else Color.Black,
                onSecondary = if (isColorDark(customSecondary)) Color.White else Color.Black,
                onBackground = Color(0xFFE2E8F0),
                onSurface = Color(0xFFF1F5F9)
            )
        }
        else -> {
            darkColorScheme(
                primary = ElegantPrimary,
                secondary = ElegantSecondary,
                background = ElegantBackground,
                surface = ElegantSurface
            )
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Helper to safely parse colors from DB hex strings
private fun safeParseColor(hex: String, fallback: Color): Color {
    return try {
        val cleaned = hex.trim().replace("#", "")
        if (cleaned.length == 6) {
            Color(android.graphics.Color.parseColor("#FF$cleaned"))
        } else if (cleaned.length == 8) {
            Color(android.graphics.Color.parseColor("#$cleaned"))
        } else {
            fallback
        }
    } catch (e: Exception) {
        fallback
    }
}

// Simple color luminance helper
fun isColorDark(color: Color): Boolean {
    val luminance = 0.2126f * color.red + 0.7152f * color.green + 0.0722f * color.blue
    return luminance < 0.5f
}
