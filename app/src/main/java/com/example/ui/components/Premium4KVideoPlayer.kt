package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import android.widget.VideoView
import android.net.Uri
import android.widget.FrameLayout
import android.view.ViewGroup
import com.example.ui.CineViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun Premium4KVideoPlayer(
    viewModel: CineViewModel,
    modifier: Modifier = Modifier
) {
    val mediaItem by viewModel.playingMedia.collectAsState()
    val audioSelected by viewModel.selectedAudio.collectAsState()
    val subtitleSelected by viewModel.selectedSubtitle.collectAsState()
    val qualitySelected by viewModel.selectedQuality.collectAsState()

    val currentMedia = mediaItem ?: return

    // Playback state and references
    var isPlayingState by remember { mutableStateOf(true) }
    var currentProgressSeconds by remember { mutableStateOf(0L) }
    var totalDurationSeconds by remember { mutableStateOf(if (currentMedia.type == com.example.data.model.MediaType.MOVIE) 7200L else 1500L) }
    
    var videoViewRef by remember { mutableStateOf<VideoView?>(null) }
    var isPrepared by remember { mutableStateOf(false) }

    // Control visibility states
    var showControls by remember { mutableStateOf(true) }
    var activeSubMenu by remember { mutableStateOf<String?>(null) } // "audio", "subtitles", "quality"

    // Set up correct video controller actions
    LaunchedEffect(isPlayingState, videoViewRef) {
        videoViewRef?.let { vv ->
            if (isPlayingState) {
                if (!vv.isPlaying) {
                    vv.start()
                }
            } else {
                if (vv.isPlaying) {
                    vv.pause()
                }
            }
        }
    }

    // Monitor playback progress
    LaunchedEffect(isPlayingState, isPrepared, videoViewRef) {
        while (isPlayingState && isPrepared) {
            videoViewRef?.let { vv ->
                if (vv.duration > 0) {
                    currentProgressSeconds = (vv.currentPosition / 1000).toLong()
                    totalDurationSeconds = (vv.duration / 1000).toLong().coerceAtLeast(1L)
                }
            }
            delay(500)
        }
    }

    // Auto-hide controls after click
    LaunchedEffect(showControls) {
        if (showControls) {
            delay(5000)
            showControls = false
        }
    }

    // Dispose the video player upon exit
    DisposableEffect(currentMedia) {
        onDispose {
            videoViewRef?.stopPlayback()
            videoViewRef = null
        }
    }

    // Main Player Canvas
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable { showControls = !showControls }
    ) {
        
        // --- REAL VIDEO PLAYER VIEW ---
        AndroidView(
            factory = { context ->
                VideoView(context).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    
                    // Set video URI (using item's videoUrl field or fallback if none)
                    val urlToPlay = if (currentMedia.videoUrl.isNotEmpty()) currentMedia.videoUrl else "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
                    setVideoURI(Uri.parse(urlToPlay))
                    
                    setOnPreparedListener { mp ->
                        totalDurationSeconds = (duration / 1000).toLong().coerceAtLeast(1L)
                        isPrepared = true
                        if (isPlayingState) {
                            start()
                        }
                    }
                    setOnCompletionListener {
                        isPlayingState = false
                        currentProgressSeconds = 0
                        seekTo(0)
                    }
                    setOnErrorListener { _, _, _ ->
                        // Suppress errors and behave nicely
                        true
                    }
                    videoViewRef = this
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { view ->
                // Handled internally through state transitions
            }
        )

        // Subtle Ambient Blur / Gradient Over Movie Player edges
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = if (showControls) 0.6f else 0.0f)
                        )
                    )
                )
        )

        // Loading Indicator while buffering/preparing
        if (!isPrepared) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = OrangePrimary, strokeWidth = 3.dp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Carregando Transmissão HD...", color = Color.White, fontSize = 14.sp)
                }
            }
        }

        // --- SCREEN CONTROLS LAYER ---
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn() + slideInVertically { -it / 5 },
            exit = fadeOut() + slideOutVertically { -it / 5 }
        ) {
            // TOP BAR OVERLAY
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Black.copy(alpha = 0.8f), Color.Transparent)
                        )
                    )
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = { viewModel.stopPlaying() },
                        modifier = Modifier.testTag("player_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = currentMedia.title,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .background(OrangePrimary, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = qualitySelected,
                                    fontSize = 10.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Legendas: $subtitleSelected  •  Áudio: $audioSelected",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // MIDPLAY BUTTONS OVERLAY (PREREWIND, FASTFORWARD, PLAY/PAUSE)
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(40.dp)
            ) {
                // Rewind 10 Seconds
                IconButton(
                    onClick = {
                        val newProgress = currentProgressSeconds - 10
                        currentProgressSeconds = if (newProgress < 0) 0 else newProgress
                        videoViewRef?.seekTo((currentProgressSeconds * 1000).toInt())
                    },
                    modifier = Modifier
                        .size(56.dp)
                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Replay10,
                        contentDescription = "Voltar 10 segundos",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Play / Pause core trigger
                IconButton(
                    onClick = { isPlayingState = !isPlayingState },
                    modifier = Modifier
                        .size(80.dp)
                        .background(OrangePrimary, CircleShape)
                        .testTag("player_play_pause_button")
                ) {
                    Icon(
                        imageVector = if (isPlayingState) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlayingState) "Pausar" else "Reproduzir",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }

                // Fast Forward 10 Seconds
                IconButton(
                    onClick = {
                        val newProgress = currentProgressSeconds + 10
                        currentProgressSeconds = if (newProgress > totalDurationSeconds) totalDurationSeconds else newProgress
                        videoViewRef?.seekTo((currentProgressSeconds * 1000).toInt())
                    },
                    modifier = Modifier
                        .size(56.dp)
                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Forward10,
                        contentDescription = "Avançar 10 segundos",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }

        // BOTTOM ACTION CONTROL BAR (TIMELINE & MENU TRIGGERS)
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn() + slideInVertically { it / 5 },
            exit = fadeOut() + slideOutVertically { it / 5 },
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f))
                        )
                    )
                    .padding(bottom = 24.dp, start = 16.dp, end = 16.dp, top = 24.dp)
            ) {
                // Progress Timeline
                val progressFraction = if (totalDurationSeconds > 0) currentProgressSeconds.toFloat() / totalDurationSeconds.toFloat() else 0f
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    Text(
                        text = formatTime(currentProgressSeconds),
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Slider(
                        value = progressFraction,
                        onValueChange = {
                            val targetSeconds = (it * totalDurationSeconds).toLong()
                            currentProgressSeconds = targetSeconds
                            videoViewRef?.seekTo((targetSeconds * 1000).toInt())
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = OrangePrimary,
                            activeTrackColor = OrangePrimary,
                            inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp)
                            .testTag("player_timeline_slider")
                    )

                    Text(
                        text = formatTime(totalDurationSeconds),
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Player configurations options trigger
                Row(
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    
                    // Audio Lang Selector trigger
                    TextButton(
                        onClick = { activeSubMenu = if (activeSubMenu == "audio") null else "audio" },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.White),
                        modifier = Modifier.testTag("audio_trigger_button")
                    ) {
                        Icon(Icons.Default.VolumeUp, contentDescription = null, tint = OrangePrimary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Áudio: ${audioSelected.substringBefore(" ")}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Subtitle Lang Selector trigger
                    TextButton(
                        onClick = { activeSubMenu = if (activeSubMenu == "subtitles") null else "subtitles" },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.White),
                        modifier = Modifier.testTag("subtitle_trigger_button")
                    ) {
                        Icon(Icons.Default.Subtitles, contentDescription = null, tint = OrangePrimary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Legendas: $subtitleSelected",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Video Quality Selector trigger
                    TextButton(
                        onClick = { activeSubMenu = if (activeSubMenu == "quality") null else "quality" },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.White),
                        modifier = Modifier.testTag("quality_trigger_button")
                    ) {
                        Icon(Icons.Default.HighQuality, contentDescription = null, tint = OrangePrimary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = qualitySelected,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // --- SUB CONFIGURATIONS POPUP SCREENS (AUDIO, SUBTITLES, RESOLUTIONS) ---
        AnimatedVisibility(
            visible = activeSubMenu != null,
            enter = fadeIn() + slideInVertically { it / 3 },
            exit = fadeOut() + slideOutVertically { it / 3 },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BlackSurface, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .border(2.dp, OrangePrimary.copy(alpha = 0.5f), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .padding(24.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when (activeSubMenu) {
                                "audio" -> "Selecionar Idioma do Áudio"
                                "subtitles" -> "Selecionar Idioma das Legendas"
                                "quality" -> "Qualidade de Transmissão (4K Atmos)"
                                else -> ""
                            },
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black
                        )
                        IconButton(onClick = { activeSubMenu = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Fechar Menu", tint = Color.White)
                        }
                    }

                    when (activeSubMenu) {
                        "audio" -> {
                            currentMedia.audioLanguages.forEach { audioOption ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.setAudioLanguage(audioOption)
                                            activeSubMenu = null
                                        }
                                        .padding(vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = audioSelected == audioOption,
                                        onClick = {
                                            viewModel.setAudioLanguage(audioOption)
                                            activeSubMenu = null
                                        },
                                        colors = RadioButtonDefaults.colors(selectedColor = OrangePrimary)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(text = audioOption, color = Color.White, fontSize = 15.sp)
                                }
                            }
                        }
                        "subtitles" -> {
                            currentMedia.subtitles.forEach { subOption ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.setSubtitle(subOption)
                                            activeSubMenu = null
                                        }
                                        .padding(vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = subtitleSelected == subOption,
                                        onClick = {
                                            viewModel.setSubtitle(subOption)
                                            activeSubMenu = null
                                        },
                                        colors = RadioButtonDefaults.colors(selectedColor = OrangePrimary)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(text = subOption, color = Color.White, fontSize = 15.sp)
                                }
                            }
                        }
                        "quality" -> {
                            listOf("4K Ultra HD (DTS)", "1080p Full HD (HDR)", "720p HD", "Qualidade Automática").forEach { qual ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.setVideoQuality(qual)
                                            activeSubMenu = null
                                        }
                                        .padding(vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = qualitySelected == qual || (qualitySelected.startsWith("4K") && qual.startsWith("4K")),
                                        onClick = {
                                            viewModel.setVideoQuality(qual)
                                            activeSubMenu = null
                                        },
                                        colors = RadioButtonDefaults.colors(selectedColor = OrangePrimary)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(text = qual, color = Color.White, fontSize = 15.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Convert seconds into standard cinematic timestamp mm:ss or hh:mm:ss
private fun formatTime(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) {
        String.format("%02d:%02d:%02d", h, m, s)
    } else {
        String.format("%02d:%02d", m, s)
    }
}
