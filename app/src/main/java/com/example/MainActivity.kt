package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.screens.MainScreen
import com.example.ui.theme.MediaPlayerTheme
import com.example.ui.viewmodel.PlayerViewModel

class MainActivity : ComponentActivity() {
    
    // Instantiate ViewModel lazily
    private val playerViewModel: PlayerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Edge-to-Edge window insets support
        enableEdgeToEdge()
        
        setContent {
            // Collect Room-persisted User Settings in real-time
            val settings by playerViewModel.userSettings.collectAsState()
            
            MediaPlayerTheme(
                themePreset = settings.themePreset,
                useDynamicColor = settings.useDynamicColor,
                customPrimaryHex = settings.customPrimaryColor,
                customSecondaryHex = settings.customSecondaryColor,
                customBackgroundHex = settings.customBackgroundColor,
                customSurfaceHex = settings.customSurfaceColor
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(viewModel = playerViewModel)
                }
            }
        }
    }
}
