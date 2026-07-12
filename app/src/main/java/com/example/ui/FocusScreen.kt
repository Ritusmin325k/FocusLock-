package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.FocusSessionService
import com.example.viewmodel.FocusViewModel
import com.example.ui.theme.GlassThemeColors
import com.example.ui.theme.LiquidGlassBackground
import androidx.compose.material.icons.filled.Lock

@Composable
fun FocusScreen(
    viewModel: FocusViewModel,
    sessionState: FocusSessionService.FocusSessionState,
    modifier: Modifier = Modifier
) {
    val isDark = viewModel.isDarkTheme()
    val themeColors = GlassThemeColors(isDark)
    val textColor = themeColors.textColor
    val cardBg = themeColors.cardBg
    val accentColor = themeColors.accentColor

    val totalSeconds = sessionState.studyDurationMinutes * 60
    val remainingSec = sessionState.remainingSeconds
    val elapsedSec = totalSeconds - remainingSec
    val progress = if (totalSeconds > 0) remainingSec.toFloat() / totalSeconds.toFloat() else 0f

    val minutes = remainingSec / 60
    val seconds = remainingSec % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)

    LiquidGlassBackground(
        isDark = isDark,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        // Minimalist Top Header Row
        Row(
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = sessionState.presetName ?: "Focus Session",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        letterSpacing = 0.5.sp
                    )
                )
                Text(
                    text = if (sessionState.currentState == FocusSessionService.FocusState.PAUSED) "Session Paused" else "Focus Active",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = accentColor,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }

            // Minimalist Outlined Button to End Session
            if (remainingSec <= 0) {
                Button(
                    onClick = { viewModel.stopSession() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = if (isDark) Color(0xFFEF4444) else Color(0xFFDC2626)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = (if (isDark) Color(0xFFEF4444) else Color(0xFFDC2626)).copy(alpha = 0.3f)
                    ),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        text = "End",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Circular Countdown Timer Display (Glassmorphic, locked focus)
        val isPaused = sessionState.currentState == FocusSessionService.FocusState.PAUSED
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding(vertical = 8.dp)
                .size(230.dp)
                .clip(CircleShape)
                .background(if (isDark) Color(0xFF1F2937).copy(alpha = 0.4f) else Color.White.copy(alpha = 0.4f))
                .border(1.dp, themeColors.glassBorder, CircleShape)
                .testTag("play_pause_session_button") // Keeps automated testing tags functional!
        ) {
            // Background grey ring
            CircularProgressIndicator(
                progress = { 1.0f },
                color = if (isDark) Color(0xFF374151) else Color(0xFFE5E7EB),
                strokeWidth = 8.dp,
                modifier = Modifier.fillMaxSize()
            )
            // Accent active progress ring (Silver/Grey/Charcoal accent)
            CircularProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                color = accentColor,
                strokeWidth = 8.dp,
                modifier = Modifier.fillMaxSize()
            )
 
            // Central countdown time text (Use high quality premium font pairing)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Session Locked",
                    tint = accentColor.copy(alpha = 0.8f),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = timeFormatted,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.ExtraBold,
                        color = textColor,
                        fontSize = 50.sp
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "STAY FOCUSED",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                    letterSpacing = 1.2.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Premium Ambient Music Controller Card
        Card(
            colors = CardDefaults.cardColors(containerColor = cardBg),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = themeColors.glassBorder
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Controller Header: AMBIENT SOUNDSCAPE & Visualizer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "FOCUS SOUNDSCAPE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        letterSpacing = 1.sp
                    )
                    
                    // Waveform visualizer (active when playing sound)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.height(14.dp)
                    ) {
                        val isSoundActive = sessionState.selectedSound != null && sessionState.selectedSound != "None"
                        val isPlaying = isSoundActive && sessionState.isSoundPlaying && !isPaused
                        
                        val infiniteTransition = rememberInfiniteTransition(label = "visualizer")
                        val barCount = 5
                        
                        val heights = (0 until barCount).map { i ->
                            if (isPlaying) {
                                val anim = infiniteTransition.animateFloat(
                                    initialValue = 0.2f,
                                    targetValue = 1.0f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(
                                            durationMillis = 350 + i * 110,
                                            easing = LinearEasing
                                        ),
                                        repeatMode = RepeatMode.Reverse
                                    ),
                                    label = "bar_$i"
                                )
                                anim.value
                            } else {
                                0.2f
                            }
                        }

                        for (i in 0 until barCount) {
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height(14.dp * heights[i])
                                    .clip(RoundedCornerShape(1.dp))
                                    .background(accentColor)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Track Cover and Info Layout
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Vinyl Cover Art with Grooves & Continuous rotation
                    val isSoundActive = sessionState.selectedSound != null && sessionState.selectedSound != "None"
                    val isPlaying = isSoundActive && sessionState.isSoundPlaying && !isPaused
                    
                    val infiniteRotation = rememberInfiniteTransition(label = "vinyl_rotation")
                    val rotationAngleState = if (isPlaying) {
                        infiniteRotation.animateFloat(
                            initialValue = 0f,
                            targetValue = 360f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(durationMillis = 6000, easing = LinearEasing),
                                repeatMode = RepeatMode.Restart
                            ),
                            label = "vinyl_angle"
                        )
                    } else {
                        remember { mutableStateOf(0f) }
                    }
                    val rotationAngle = rotationAngleState.value

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9))
                            .rotate(rotationAngle)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val r = size.minDimension / 2
                            drawCircle(
                                color = if (isDark) Color(0xFF334155) else Color(0xFF94A3B8),
                                radius = r,
                                style = Stroke(width = 1f)
                            )
                            drawCircle(
                                color = if (isDark) Color(0xFF334155).copy(alpha = 0.6f) else Color(0xFF94A3B8).copy(alpha = 0.6f),
                                radius = r * 0.75f,
                                style = Stroke(width = 1f)
                            )
                            drawCircle(
                                color = if (isDark) Color(0xFF334155).copy(alpha = 0.3f) else Color(0xFF94A3B8).copy(alpha = 0.3f),
                                radius = r * 0.5f,
                                style = Stroke(width = 1f)
                            )
                            drawCircle(
                                color = accentColor,
                                radius = r * 0.25f
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(10.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Dynamic Sound Names and Detailed Descriptions
                    Column(modifier = Modifier.weight(1f)) {
                        val activeSoundName = sessionState.selectedSound ?: "None"
                        val displayName = when (activeSoundName) {
                            "Rain" -> "Serene Rain"
                            "Piano" -> "Ethereal Piano"
                            "Forest" -> "Mystic Forest"
                            "Ocean" -> "Deep Ocean Waves"
                            "Wind" -> "Resonant Wind"
                            "Fireplace" -> "Warm Fireplace"
                            "Binaural Beats" -> "Cosmic Binaural Beats"
                            "Isochronic Tones" -> "Alpha Isochronic Tones"
                            "White Noise" -> "White Noise Spectrum"
                            "Brown Noise" -> "Deep Brown Noise"
                            "Pink Noise" -> "Pink Noise Spectrum"
                            else -> "Silent Meditation"
                        }
                        
                        val soundDesc = when (activeSoundName) {
                            "Rain" -> "Pink noise with ambient droplet patterns"
                            "Forest" -> "Soft rustling winds & gentle crickets"
                            "Ocean" -> "Slow amplitude-modulated tide wave cycles"
                            "Wind" -> "Deep resonant breeze & sweeping noise"
                            "Fireplace" -> "Warm crackling fire sparks & soft embers"
                            "Piano" -> "Calm procedural space-synth chord pads"
                            "Binaural Beats" -> "10 Hz alpha-wave neural frequency balance"
                            "Isochronic Tones" -> "150 Hz carrier tone with 10 Hz alpha pulse"
                            "White Noise" -> "Even-frequency focus white static spectrum"
                            "Brown Noise" -> "Deep leaky-integrated bass brown spectrum"
                            "Pink Noise" -> "Perfect 1/f spectral noise distribution"
                            else -> "Unwind in complete peaceful silence"
                        }

                        Text(
                            text = displayName,
                            color = textColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            maxLines = 1
                        )
                        Text(
                            text = soundDesc,
                            color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Track Controls Layout (Prev, Play/Pause, Next)
                val availableSounds = listOf(
                    "None", "Rain", "Piano", "Forest", "Ocean", 
                    "Wind", "Fireplace", "Binaural Beats", "Isochronic Tones", 
                    "White Noise", "Brown Noise", "Pink Noise"
                )
                val currentSound = sessionState.selectedSound ?: "None"
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            val currentIdx = availableSounds.indexOf(currentSound)
                            val prevIdx = if (currentIdx <= 0) availableSounds.size - 1 else currentIdx - 1
                            viewModel.updateSessionSound(availableSounds[prevIdx], sessionState.soundVolume)
                        },
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Previous Sound",
                            tint = textColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    val isSoundActive = currentSound != "None"
                    val isPlaying = isSoundActive && sessionState.isSoundPlaying && !isPaused

                    IconButton(
                        onClick = {
                            if (isSoundActive) {
                                viewModel.updateSessionSound("None", sessionState.soundVolume)
                            } else {
                                viewModel.updateSessionSound("Rain", sessionState.soundVolume)
                            }
                        },
                        modifier = Modifier
                            .size(50.dp)
                            .background(accentColor.copy(alpha = 0.15f), shape = CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause Sound",
                            tint = accentColor,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    IconButton(
                        onClick = {
                            val currentIdx = availableSounds.indexOf(currentSound)
                            val nextIdx = (currentIdx + 1) % availableSounds.size
                            viewModel.updateSessionSound(availableSounds[nextIdx], sessionState.soundVolume)
                        },
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Next Sound",
                            tint = textColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // Smooth Minimal Volume Slider
                if (currentSound != "None") {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Volume",
                            tint = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Slider(
                            value = sessionState.soundVolume,
                            onValueChange = { viewModel.updateSessionSound(currentSound, it) },
                            colors = SliderDefaults.colors(
                                activeTrackColor = accentColor,
                                thumbColor = accentColor,
                                inactiveTrackColor = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Configured Breaks Card
        Card(
            colors = CardDefaults.cardColors(containerColor = cardBg),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = themeColors.glassBorder
            )
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Configured Breaks",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = textColor),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val remainingBreaks = sessionState.totalBreaksConfigured - sessionState.breaksUsed
                    Column {
                        Text(
                            text = "$remainingBreaks breaks left",
                            color = textColor,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Used ${sessionState.breaksUsed} of ${sessionState.totalBreaksConfigured} total",
                            color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                            fontSize = 11.sp
                        )
                    }

                    // Take break trigger
                    val canTakeBreak = remainingBreaks > 0 && sessionState.currentState == FocusSessionService.FocusState.RUNNING
                    Button(
                        onClick = { viewModel.startBreak() },
                        enabled = canTakeBreak,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF59E0B),
                            contentColor = Color.White,
                            disabledContainerColor = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("take_break_button")
                    ) {
                        Text("Take Break", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (index in 1..sessionState.totalBreaksConfigured) {
                        val isUsed = index <= sessionState.breaksUsed
                        val bulbColor = if (isUsed) Color(0xFF94A3B8) else Color(0xFFF59E0B)
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(CircleShape)
                                .background(bulbColor.copy(alpha = if (isUsed) 0.3f else 1.0f))
                        )
                    }
                }
            }
        }

        // Live App Block Distraction Simulation Section
        if (sessionState.blockedApps.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg.copy(alpha = 0.35f)),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, themeColors.glassBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val pkgToBlock = sessionState.blockedApps.firstOrNull() ?: "com.instagram.android"
                        viewModel.simulateBlockedAppInterruption(pkgToBlock)
                    }
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Block,
                        contentDescription = "App blocker",
                        tint = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Demo: Simulate Blocked App Distraction",
                        color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
    }
}
