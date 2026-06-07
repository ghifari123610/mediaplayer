package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.SettingsEntity
import com.example.domain.model.TrackItem
import com.example.ui.viewmodel.PlayerViewModel
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: PlayerViewModel) {
    val settings by viewModel.userSettings.collectAsState()
    val playlist by viewModel.playlist.collectAsState()
    val spectrum by viewModel.spectrumData.collectAsState()
    
    var activeTab by remember { mutableStateOf("player") } // player, dsp, theme, docs

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MusicNote,
                            contentDescription = "App Icon Logo",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = "Custom Media Player",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            letterSpacing = 0.5.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = activeTab == "player",
                    onClick = { activeTab = "player" },
                    icon = { Icon(Icons.Filled.LibraryMusic, contentDescription = "Tab Pemutar") },
                    label = { Text("Pemutar") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("tab_player")
                )
                NavigationBarItem(
                    selected = activeTab == "dsp",
                    onClick = { activeTab = "dsp" },
                    icon = { Icon(Icons.Filled.Equalizer, contentDescription = "Tab Audio DSP") },
                    label = { Text("EQ & Gestur") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("tab_dsp")
                )
                NavigationBarItem(
                    selected = activeTab == "theme",
                    onClick = { activeTab = "theme" },
                    icon = { Icon(Icons.Filled.Palette, contentDescription = "Tab Tema") },
                    label = { Text("Tema") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("tab_theme")
                )
                NavigationBarItem(
                    selected = activeTab == "docs",
                    onClick = { activeTab = "docs" },
                    icon = { Icon(Icons.Filled.Book, contentDescription = "Tab Panduan Dev") },
                    label = { Text("Dev Hub") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("tab_docs")
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Animated Transition between panels
            AnimatedContent(
                targetState = activeTab,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                },
                label = "TabTransition"
            ) { targetTab ->
                when (targetTab) {
                    "player" -> PlayerTab(viewModel, settings, playlist, spectrum)
                    "dsp" -> DspTab(viewModel, settings)
                    "theme" -> ThemeTab(viewModel, settings)
                    "docs" -> DeveloperDocsTab()
                }
            }
        }
    }
}

// ---------------------------------------------------------
// 1. PLAYER TAB PANELS
// ---------------------------------------------------------
@Composable
fun PlayerTab(
    viewModel: PlayerViewModel,
    settings: SettingsEntity,
    playlist: List<TrackItem>,
    spectrum: List<Float>
) {
    val currentTrack = viewModel.currentTrack()
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    if (isTablet) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Left column: Active Media Cover & Controls
            Box(
                modifier = Modifier
                    .weight(1.2f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                ActivePlayerColumn(viewModel, settings, currentTrack, spectrum)
            }

            // Right column: Track Explorer List
            val viewModeTag = if (settings.isGridView) "grid" else "list"
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Antrean Putar (${playlist.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    IconButton(
                        onClick = { viewModel.setGridView(!settings.isGridView) },
                        modifier = Modifier.testTag("btn_layout_toggle")
                    ) {
                        Icon(
                            if (settings.isGridView) Icons.Filled.ViewList else Icons.Filled.GridView,
                            contentDescription = "Toggle Grid Layout"
                        )
                    }
                }

                if (settings.isGridView) {
                    PlaylistGridView(playlist, viewModel.currentTrackIndex, onSelected = { viewModel.playTrack(it) })
                } else {
                    PlaylistListView(playlist, viewModel.currentTrackIndex, onSelected = { viewModel.playTrack(it) })
                }
            }
        }
    } else {
        // Mobile layout: Vertical Stack scrollable at the playlist
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ActivePlayerColumn(viewModel, settings, currentTrack, spectrum)
            
            Spacer(modifier = Modifier.height(8.dp))

            // Playlist title header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Daftar Lagu (${playlist.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                IconButton(
                    onClick = { viewModel.setGridView(!settings.isGridView) },
                    modifier = Modifier.testTag("btn_layout_toggle")
                ) {
                    Icon(
                        if (settings.isGridView) Icons.Filled.ViewList else Icons.Filled.GridView,
                        contentDescription = "Toggle Grid Layout"
                    )
                }
            }

            // Constraints Box to safely hold list/grid size heights in scrollable view
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
            ) {
                if (settings.isGridView) {
                    PlaylistGridView(playlist, viewModel.currentTrackIndex, onSelected = { viewModel.playTrack(it) })
                } else {
                    PlaylistListView(playlist, viewModel.currentTrackIndex, onSelected = { viewModel.playTrack(it) })
                }
            }
        }
    }
}

@Composable
fun ActivePlayerColumn(
    viewModel: PlayerViewModel,
    settings: SettingsEntity,
    currentTrack: TrackItem?,
    spectrum: List<Float>
) {
    if (currentTrack == null) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            Text("Memuat Playlist...", modifier = Modifier.padding(top = 12.dp))
        }
        return
    }

    var showGestureHint by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Core Centerpiece: Cover Art / Video canvas with Drag gesture listening
        Box(
            modifier = Modifier
                .size(settings.coverArtSize.dp)
                .clip(RoundedCornerShape(24.dp))
                .shadow(16.dp, RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surface)
                .pointerInput(
                    settings.gestureVolumeEnabled, 
                    settings.gestureBrightnessEnabled, 
                    settings.gestureSeekEnabled
                ) {
                    detectDragGestures(
                        onDragStart = { showGestureHint = false },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val width = size.width
                            val height = size.height
                            val touchX = change.position.x

                            // Vertical gesture on left side -> brightness
                            // Vertical gesture on right side -> volume
                            // Horizontal gesture globally -> seek
                            if (kotlin.math.abs(dragAmount.x) > kotlin.math.abs(dragAmount.y)) {
                                if (settings.gestureSeekEnabled) {
                                    val seekDelta = (dragAmount.x / width * 30000).toLong() // drag across screen is ~30s
                                    viewModel.performSeekSwipe(seekDelta)
                                }
                            } else {
                                if (touchX < width / 2f) {
                                    if (settings.gestureBrightnessEnabled) {
                                        viewModel.adjustBrightnessBySwipe(-dragAmount.y / height)
                                    }
                                } else {
                                    if (settings.gestureVolumeEnabled) {
                                        viewModel.adjustVolumeBySwipe(-dragAmount.y / height)
                                    }
                                }
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            if (currentTrack.isVideo) {
                // Representing ExoPlayer video stream using beautifully active Custom Math Canvas!
                VideoSimulationCanvas(isPlaying = viewModel.isPlaying)
            } else {
                // Procedural Gradient Cover Art aligned with design guidelines
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    safeParseRGB(currentTrack.iconGradientStart),
                                    safeParseRGB(currentTrack.iconGradientEnd)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MusicNote,
                            contentDescription = "Music Playing",
                            tint = Color.White.copy(alpha = 0.82f),
                            modifier = Modifier.size(settings.coverArtSize.dp / 3)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = currentTrack.album,
                            color = Color.White.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Spectrum graphic visualizer overlay (dancing curves) at the bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.65f))
                        )
                    )
            ) {
                SpectrumVisualizer(spectrum = spectrum, color = MaterialTheme.colorScheme.primary)
            }

            // Gesture Interactive Info Overlay Indicator
            androidx.compose.animation.AnimatedVisibility(
                visible = showGestureHint,
                enter = fadeIn() + expandIn(),
                exit = fadeOut(),
                modifier = Modifier.padding(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.65f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        "← Geser Pad untuk Volume/Kecerahan/Detik →",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Active State Status HUD: displays brightness / volume adjustments on touch
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(Icons.Filled.VolumeUp, contentDescription = "Active Volume Icon", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                Text(
                    text = "Vol: ${(viewModel.activeVolume * 100).toInt()}%",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }
            if (currentTrack.isVideo) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Filled.LightMode, contentDescription = "Active Brightness Icon", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    Text(
                        text = "Kecerahan: ${(viewModel.activeBrightness * 100).toInt()}%",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }
            }
        }

        // Track Details Metadata Panel
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            Text(
                text = currentTrack.title,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 26.sp
            )
            Text(
                text = "${currentTrack.artist} • ${currentTrack.album}",
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Playback Position Track Slider Timeline
        val progress = if (currentTrack.durationSeconds > 0) {
            viewModel.playbackPositionMs.toFloat() / (currentTrack.durationSeconds * 1000f)
        } else 0f

        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
            Slider(
                value = progress,
                onValueChange = {
                    val targetMs = (it * currentTrack.durationSeconds * 1000).toLong()
                    viewModel.seekToMs(targetMs)
                },
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.24f)
                ),
                modifier = Modifier.fillMaxWidth().testTag("player_progress_slider")
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatMs(viewModel.playbackPositionMs),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
                Text(
                    text = currentTrack.durationText,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
        }

        // Standard Navigation Media Players Controller Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.previousTrack() },
                modifier = Modifier.size(56.dp).testTag("btn_prev")
            ) {
                Icon(
                    Icons.Filled.SkipPrevious,
                    contentDescription = "Previous Track",
                    modifier = Modifier.size(36.dp),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))

            // Play / Pause Floating dynamic action bar
            FloatingActionButton(
                onClick = { viewModel.togglePlayPause() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                modifier = Modifier.size(64.dp).testTag("btn_play_pause")
            ) {
                Icon(
                    imageVector = if (viewModel.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (viewModel.isPlaying) "Pause" else "Play",
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            IconButton(
                onClick = { viewModel.nextTrack() },
                modifier = Modifier.size(56.dp).testTag("btn_next")
            ) {
                Icon(
                    Icons.Filled.SkipNext,
                    contentDescription = "Next Track",
                    modifier = Modifier.size(36.dp),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

// ---------------------------------------------------------
// 2. PLAYLIST VIEWS (GRID VS LIST)
// ---------------------------------------------------------
@Composable
fun PlaylistListView(
    playlist: List<TrackItem>,
    currentIndex: Int,
    onSelected: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(playlist) { index, track ->
            val isActive = index == currentIndex
            val borderBrush = if (isActive) {
                BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
            } else null

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelected(index) }
                    .testTag("track_item_$index"),
                border = borderBrush,
                colors = CardDefaults.cardColors(
                    containerColor = if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                    else MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Small thumbnail icon with procedural gradient colors
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        safeParseRGB(track.iconGradientStart),
                                        safeParseRGB(track.iconGradientEnd)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (track.isVideo) Icons.Filled.Movie else Icons.Filled.MusicNote,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = track.title,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 14.sp,
                            color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = track.artist,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Text(
                        text = track.durationText,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
fun PlaylistGridView(
    playlist: List<TrackItem>,
    currentIndex: Int,
    onSelected: (Int) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        itemsIndexed(playlist) { index, track ->
            val isActive = index == currentIndex
            val borderBrush = if (isActive) {
                BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
            } else null

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelected(index) }
                    .testTag("track_grid_item_$index"),
                border = borderBrush,
                colors = CardDefaults.cardColors(
                    containerColor = if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                    else MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        safeParseRGB(track.iconGradientStart),
                                        safeParseRGB(track.iconGradientEnd)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (track.isVideo) Icons.Filled.Movie else Icons.Filled.MusicNote,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = track.title,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp,
                            color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = track.artist,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------
// 3. DSP & EQUALIZER CONTROL PANEL
// ---------------------------------------------------------
@Composable
fun DspTab(viewModel: PlayerViewModel, settings: SettingsEntity) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Audio DSP & Equalizer",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onBackground
        )

        // Equalizer FX Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Equalizer 5-Band", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Sesuaikan frekuensi audio individual", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                    Button(
                        onClick = {
                            viewModel.updateEqualizer(0, 0f)
                            viewModel.updateEqualizer(1, 0f)
                            viewModel.updateEqualizer(2, 0f)
                            viewModel.updateEqualizer(3, 0f)
                            viewModel.updateEqualizer(4, 0f)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), contentColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Reset Flat", fontSize = 12.sp)
                    }
                }

                // Vertical Sliders representation of active Equalizer
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val bands = listOf(
                        Pair("60 Hz", settings.eq60Hz),
                        Pair("230 Hz", settings.eq230Hz),
                        Pair("910 Hz", settings.eq910Hz),
                        Pair("4 kHz", settings.eq4kHz),
                        Pair("14 kHz", settings.eq14kHz)
                    )

                    bands.forEachIndexed { index, (label, value) ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(56.dp)
                        ) {
                            Text(
                                text = "${value.toInt()} dB",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            
                            // Native Rotated Column representation for slider verticality
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Slider(
                                    value = value,
                                    onValueChange = { viewModel.updateEqualizer(index, it) },
                                    valueRange = -10f..10f,
                                    modifier = Modifier
                                        .height(140.dp)
                                        .drawBehind {
                                            // Optional guide lines
                                        }
                                )
                            }

                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }

        // Bass Boost Panel Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Bass Boost (Efek Rendah)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Meningkatkan rentang frekuensi bass (sub-woofer)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                    Text(
                        text = "${(settings.bassBoost * 10).toInt()}%",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 18.sp
                    )
                }

                Slider(
                    value = settings.bassBoost,
                    onValueChange = { viewModel.updateBassBoost(it) },
                    valueRange = 0f..10f,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("bass_boost_slider")
                )
            }
        }

        // Gestures Customization Controls Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Kustomisasi Kontrol Gestur", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(
                    text = "Aktifkan atau matikan swipe gestur pada area cover art pemutar.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Gestur Detik (Horizontal Seek)", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text("Geser ke kiri/kanan untuk mencari durasi lagu", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    }
                    Switch(
                        checked = settings.gestureSeekEnabled,
                        onCheckedChange = { viewModel.toggleGestureSeek(it) },
                        modifier = Modifier.testTag("switch_gesture_seek")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Gestur Volume (Tepi Kanan)", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text("Geser ke atas/bawah pada wilayah kanan cover art", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    }
                    Switch(
                        checked = settings.gestureVolumeEnabled,
                        onCheckedChange = { viewModel.toggleGestureVolume(it) },
                        modifier = Modifier.testTag("switch_gesture_volume")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Gestur Kecerahan (Tepi Kiri)", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text("Geser ke atas/bawah pada wilayah kiri cover art (Mode Video)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    }
                    Switch(
                        checked = settings.gestureBrightnessEnabled,
                        onCheckedChange = { viewModel.toggleGestureBrightness(it) },
                        modifier = Modifier.testTag("switch_gesture_brightness")
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------
// 4. THEME & COLOR PRESETTINGS PANEL
// ---------------------------------------------------------
@Composable
fun ThemeTab(viewModel: PlayerViewModel, settings: SettingsEntity) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Kustomisasi Tema",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onBackground
        )

        // Dynamic Color (Material You) Toggle Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Dynamic Color (Material You)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Gunakan skema warna dari wallpaper Android Anda (Android 12+)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
                Switch(
                    checked = settings.useDynamicColor,
                    onCheckedChange = { viewModel.toggleDynamicColor(it) },
                    modifier = Modifier.testTag("switch_dynamic_color")
                )
            }
        }

        // Default Presets Grid Layout
        if (!settings.useDynamicColor) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Tema Presets", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Pilih palet warna kustom berdesain premium", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))

                    val presets = listOf(
                        Triple("elegant_dark", "Elegant Dark", Color(0xFFD0BCFF)),
                        Triple("dark_cosmic", "Midnight Cosmic", Color(0xFF9d4edd)),
                        Triple("warm_amber", "Warm Amber", Color(0xFFFF9F1C)),
                        Triple("emerald_oasis", "Emerald Oasis", Color(0xFF38B000)),
                        Triple("minimal_slate", "Slate Minimalist", Color(0xFFE2E8F0)),
                        Triple("custom", "Warna Kustom", safeParseRGB(settings.customPrimaryColor))
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        presets.forEach { (id, title, primaryClr) ->
                            val isSelected = settings.themePreset == id
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                        else Color.Transparent
                                    )
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { viewModel.updateThemePreset(id) }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(primaryClr)
                                    )
                                    Text(
                                        text = title,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 14.sp
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        Icons.Filled.Check,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Dynamic Interactive Color Customizer Palette (Only active if custom preset selected)
        if (settings.themePreset == "custom" && !settings.useDynamicColor) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Custom Theme Builder (Realtime)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(
                        "Ubah dan simpan warna primer/sekunder player secara langsung di database Room.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )

                    val colorOptions = listOf(
                        Quadruple("Royal Purple", "#8A2BE2", "#00FFFF", "#0B061A", "#140D2B"),
                        Quadruple("Cyberpunk Neon", "#FF007F", "#00FFDD", "#0F0018", "#1E002B"),
                        Quadruple("Crimson Volcano", "#E63946", "#F1FAEE", "#1D3557", "#457B9D"),
                        Quadruple("Ocean Breeze", "#0077B6", "#90E0EF", "#03045E", "#0077B6"),
                        Quadruple("Forest Gold", "#2D6A4F", "#D8F3DC", "#081C15", "#1B4332")
                    )

                    Text("Pilih Palet Dasar:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        colorOptions.forEach { (name, prim, sec, bg, surf) ->
                            Button(
                                onClick = { viewModel.updateCustomColors(prim, sec, bg, surf) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = safeParseRGB(bg),
                                    contentColor = Color.White
                                ),
                                border = BorderStroke(1.dp, safeParseRGB(prim)),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(safeParseRGB(prim)))
                                    Text(name, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }

                    // Cover Art Resizing Controls
                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Ukuran Cover Art di Pemutar", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text("${settings.coverArtSize.toInt()} dp", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Slider(
                        value = settings.coverArtSize,
                        onValueChange = { viewModel.updateCoverArtSize(it) },
                        valueRange = 150f..300f,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("slider_cover_size")
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------
// 5. TECHNICAL DOCUMENTATION HUB SCREEN
// ---------------------------------------------------------
@Composable
fun DeveloperDocsTab() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Panduan Arsitektur & Implementasi Dev",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Saran Arsitektur Clean, struktur proyek, kustomisasi media, dan contoh code.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }

        // Clean Architecture and MVVM Overview Card
        item {
            DocPanel(
                heading = "1. Clean Architecture + MVVM",
                body = "Struktur arsitektur yang kuat memisahkan kode menjadi 3 lapisan independen:\n\n" +
                        "✦ Presentation Layer (UI):\n" +
                        "  - ViewModel: Membaca pengaturan user secara reaktif dari domain (Flow) dan menyimpan state pemutaran.\n" +
                        "  - Compose Views: Mengikuti state visual Material 3 yg dideklarasikan.\n\n" +
                        "✦ Domain Layer (Logic):\n" +
                        "  - Models: Objek entitas media murni (TrackItem).\n" +
                        "  - Use Cases & Repository Interfaces: Mendeklarasikan kontrak operasi file/database.\n\n" +
                        "✦ Data Layer (Frameworks):\n" +
                        "  - Room Database: Menyimpan relasi playlist, entitas preferensi lagu secara local.\n" +
                        "  - ExoPlayer (Media3) Service: Berinteraksi dengan hardware audio dan background core."
            )
        }

        // Folder Structure Blueprint Custom Card
        item {
            DocPanel(
                heading = "2. Blueprint Struktur Folder Proyek",
                body = "Gunakan template bersih ini di project Android Anda:\n" +
                        "📂 app\n" +
                        " ┣ 📂 src/main/java/com/example\n" +
                        " ┃ ┣ 📂 data\n" +
                        " ┃ ┃ ┣ 📂 database    // Entities, AppDatabase, SettingsDao\n" +
                        " ┃ ┃ ┣ 📂 repository  // Implementasi MediaRepositoryImpl\n" +
                        " ┃ ┃ ┗ 📂 service     // Media3 Playback Service & Notification manager\n" +
                        " ┃ ┣ 📂 domain\n" +
                        " ┃ ┃ ┣ 📂 model       // Domain entities (Track, Theme)\n" +
                        " ┃ ┃ ┗ 📂 repository  // Interfaces kontrak media & settings\n" +
                        " ┃ ┣ 📂 ui\n" +
                        " ┃ ┃ ┣ 📂 screens     // MainScreen, PlayerView, SettingsView\n" +
                        " ┃ ┃ ┣ 📂 theme       // Color, Theme, Type\n" +
                        " ┃ ┃ ┗ 📂 viewmodel   // PlayerViewModel (MVVM Controller)\n" +
                        " ┃ ┗ 📜 MainActivity.kt"
            )
        }

        // ExoPlayer & Notification Code Card
        item {
            DocPanel(
                heading = "3. ExoPlayer & Background Playback (Media3)",
                body = "Implementasikan background service menggunakan MediaSessionService:\n\n" +
                        "// File: service/PlaybackService.kt\n" +
                        "class PlaybackService : MediaSessionService() {\n" +
                        "    private var mediaSession: MediaSession? = null\n\n" +
                        "    override fun onCreate() {\n" +
                        "        super.onCreate()\n" +
                        "        val player = ExoPlayer.Builder(this).build()\n" +
                        "        mediaSession = MediaSession.Builder(this, player).build()\n" +
                        "    }\n\n" +
                        "    override fun onGetSession(controllerInfo: ControllerInfo): MediaSession? {\n" +
                        "        return mediaSession\n" +
                        "    }\n\n" +
                        "    override fun onDestroy() {\n" +
                        "        mediaSession?.player?.release()\n" +
                        "        mediaSession?.release()\n" +
                        "        super.onDestroy()\n" +
                        "    }\n" +
                        "}\n\n" +
                        "✦ PENTING: Daftarkan PlaybackService di AndroidManifest.xml dengan intent action 'media3.MediaSessionService' dan mintak runtime permission FOREGROUND_SERVICE_MEDIA_PLAYBACK."
            )
        }

        // Jetpack Compose Custom Theme Engine Snippet
        item {
            DocPanel(
                heading = "4. Code Snippet: Mulai Theme di Compose",
                body = "Gunakan kode dasar ini untuk memicu kustomisasi dinamis:\n\n" +
                        "@Composable\n" +
                        "fun DynamicallyCustomTheme(\n" +
                        "    settings: SettingsEntity,\n" +
                        "    content: @Composable () -> Unit\n" +
                        ") {\n" +
                        "    val dynamicColor = settings.useDynamicColor && SDK_INT >= S\n" +
                        "    val activeScheme = if (dynamicColor) {\n" +
                        "        dynamicDarkColorScheme(LocalContext.current)\n" +
                        "    } else {\n" +
                        "        darkColorScheme(\n" +
                        "            primary = Color(parseColor(settings.customPrimaryColor)),\n" +
                        "            secondary = Color(parseColor(settings.customSecondaryColor)),\n" +
                        "            background = Color(parseColor(settings.customBackgroundColor))\n" +
                        "        )\n" +
                        "    }\n" +
                        "    MaterialTheme(colorScheme = activeScheme, content = content)\n" +
                        "}"
            )
        }

        // Audio Effects & Gestures Setup Guide
        item {
            DocPanel(
                heading = "5. Audio Effects & Gesture Controllers",
                body = "✦ Equalizer & Bass Boost:\n" +
                        "ExoPlayer mengizinkan penyisipan filter audio audioSessionId. Gunakan subclass AudioEffect:\n" +
                        "  val equalizer = Equalizer(0, audioSessionId)\n" +
                        "  equalizer.enabled = true\n" +
                        "  equalizer.setBandLevel(0, band0Level) // 60Hz\n" +
                        "  val boost = BassBoost(0, audioSessionId).apply { enabled = true; setStrength(1000) }\n\n" +
                        "✦ Swipe Gestures in Compose:\n" +
                        "Gunakan Modifier.pointerInput() untuk menghitung pixel geser. Hitung dragAmount.y untuk memperbarui volume atau kecerahan layer layar Android secara proporsional."
            )
        }
    }
}

@Composable
fun DocPanel(heading: String, body: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = heading,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = body,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                fontFamily = FontFamily.SansSerif,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                textAlign = TextAlign.Start
            )
        }
    }
}

// ---------------------------------------------------------
// 6. DETAILED GRAPHICAL DESIGN CANVAS DRAWERS & HELPERS
// ---------------------------------------------------------
@Composable
fun VideoSimulationCanvas(isPlaying: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "VideoVisualizer")
    
    // Wave oscillator math angles
    val angleRad1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RadWave1"
    )

    val angleRad2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RadWave2"
    )

    val gradientPrimary = MaterialTheme.colorScheme.primary
    val gradientSecondary = MaterialTheme.colorScheme.secondary

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        val width = size.width
        val height = size.height

        // Draw layered fluid landscape paths (simulation sound-waves in spatial cosmos)
        val path1 = androidx.compose.ui.graphics.Path()
        val path2 = androidx.compose.ui.graphics.Path()

        path1.moveTo(0f, height / 2)
        path2.moveTo(0f, height / 2)

        val steps = 40
        for (i in 0..steps) {
            val progress = i.toFloat() / steps
            val x = progress * width

            // Generate oscillator dynamic heights using sin/cos functions depending on Play state
            val playFactor = if (isPlaying) 1.0f else 0.15f
            val y1 = height / 2 + sin(progress * 3 * Math.PI + angleRad1).toFloat() * 60f * playFactor
            val y2 = height / 2 + sin(progress * 5 * Math.PI - angleRad2).toFloat() * 40f * playFactor

            path1.lineTo(x, y1)
            path2.lineTo(x, y2)
        }

        // Render waves onto the Video canvas
        drawPath(
            path = path1,
            brush = Brush.horizontalGradient(
                colors = listOf(gradientPrimary, gradientSecondary)
            ),
            style = Stroke(width = 4.dp.toPx())
        )

        drawPath(
            path = path2,
            brush = Brush.horizontalGradient(
                colors = listOf(gradientSecondary, gradientPrimary)
            ),
            style = Stroke(width = 3.dp.toPx())
        )

        // Cosmic central star representing focus
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White, Color.Transparent),
                center = Offset(width / 2, height / 2),
                radius = if (isPlaying) 35.dp.toPx() else 15.dp.toPx()
            ),
            radius = if (isPlaying) 40.dp.toPx() else 20.dp.toPx(),
            center = Offset(width / 2, height / 2)
        )
    }
}

@Composable
fun SpectrumVisualizer(spectrum: List<Float>, color: Color) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val barCount = spectrum.size
        val gap = 6.dp.toPx()
        val totalGaps = gap * (barCount - 1)
        val barWidth = (width - totalGaps) / barCount

        for (i in 0 until barCount) {
            val value = spectrum[i]
            val barHeight = value * height
            val x = i * (barWidth + gap)
            val y = height - barHeight

            drawRoundRect(
                color = color.copy(alpha = 0.85f),
                topLeft = Offset(x, y),
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )
        }
    }
}

// Color Utility
private fun safeParseRGB(hex: String): Color {
    return try {
        val cleaned = hex.trim().replace("#", "")
        if (cleaned.length == 6) {
            Color(android.graphics.Color.parseColor("#FF$cleaned"))
        } else if (cleaned.length == 8) {
            Color(android.graphics.Color.parseColor("#$cleaned"))
        } else {
            Color(0xFF9d4edd)
        }
    } catch (e: Exception) {
        Color(0xFF9d4edd)
    }
}

// Helper: formats milliseconds to "M:SS" or "MM:SS"
fun formatMs(ms: Long): String {
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return String.format("%d:%02d", min, sec)
}

// 4-tuple utility
data class Quadruple<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)
