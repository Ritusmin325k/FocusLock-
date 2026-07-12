package com.example.ui

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalActivity
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueryBuilder
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SessionEntity
import com.example.viewmodel.FocusViewModel
import com.example.ui.theme.GlassThemeColors
import com.example.ui.theme.LiquidGlassBackground
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import com.example.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: FocusViewModel,
    stats: com.example.viewmodel.StatisticsSummary?,
    onStartSessionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = viewModel.isDarkTheme()
    val themeColors = GlassThemeColors(isDark)
    val textColor = themeColors.textColor
    val cardBg = themeColors.cardBg
    val accentColor = themeColors.accentColor
    val flameColor = Color(0xFFF97316) // Keep flame orange for gamification/streaks

    LiquidGlassBackground(
        isDark = isDark,
        modifier = modifier
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
        // App title header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_logo_f_minimalist_1783876202796),
                        contentDescription = "FocusLock Logo",
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, themeColors.glassBorder, RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Column {
                        Text(
                            text = "FocusLock",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = textColor,
                                letterSpacing = 0.5.sp
                            )
                        )
                        Text(
                            text = "Focus first. Breaks on your terms.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                            )
                        )
                    }
                }
                
                // Streak Badge on top right
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(flameColor.copy(alpha = 0.15f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = "Flame Icon",
                        tint = flameColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${stats?.currentStreak ?: 0}d Streak",
                        color = flameColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // Daily Goal Progress Ring Card
        item {
            val dailyGoalMin = viewModel.getDailyGoalMinutes()
            val todayFocusMin = stats?.todayFocusMinutes ?: 0
            val progress = if (dailyGoalMin > 0) todayFocusMin.toFloat() / dailyGoalMin.toFloat() else 0f
            val percent = (progress * 100).toInt().coerceIn(0, 100)

            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, themeColors.glassBorder),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Daily Goal Progress",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "$todayFocusMin min focused today",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = accentColor
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Goal is $dailyGoalMin min",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                            )
                        )
                    }

                    // Circular goal ring
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(90.dp)
                    ) {
                        CircularProgressIndicator(
                            progress = { 1.0f },
                            color = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0),
                            strokeWidth = 8.dp,
                            modifier = Modifier.fillMaxSize()
                        )
                        CircularProgressIndicator(
                            progress = { progress.coerceIn(0f, 1f) },
                            color = accentColor,
                            strokeWidth = 8.dp,
                            modifier = Modifier.fillMaxSize()
                        )
                        Text(
                            text = "$percent%",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                        )
                    }
                }
            }
        }

        // Action Button: Start Focus Session Card (Hero Silver/Charcoal Gradient)
        item {
            val heroContentColor = if (isDark) Color(0xFF111827) else Color.White
            val buttonGradient = if (isDark) {
                Brush.horizontalGradient(
                    listOf(Color(0xFFE5E7EB), Color(0xFFD1D5DB), Color(0xFF9CA3AF))
                )
            } else {
                Brush.horizontalGradient(
                    listOf(Color(0xFF374151), Color(0xFF1F2937), Color(0xFF111827))
                )
            }
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("start_session_trigger_card")
                    .clickable { onStartSessionClick() },
                shape = RoundedCornerShape(24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(buttonGradient)
                        .padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Start Focus Session",
                                color = heroContentColor,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Choose duration, configure breaks, and silence distracting apps.",
                                color = heroContentColor.copy(alpha = 0.85f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(heroContentColor.copy(alpha = 0.2f), shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Arrow right icon",
                                tint = heroContentColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }

        // Stats Overview Grid Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Total focused hours
                Card(
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, themeColors.glassBorder),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Icon(
                            imageVector = Icons.Default.QueryBuilder,
                            contentDescription = "Timer icon",
                            tint = accentColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = String.format(Locale.US, "%.1f hrs", stats?.totalFocusHours ?: 0f),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                        )
                        Text(
                            text = "Total Focus Time",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                            )
                        )
                    }
                }

                // Total sessions
                Card(
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, themeColors.glassBorder),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = "Trends icon",
                            tint = Color(0xFF10B981), // Emerald Green
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "${stats?.totalSessionsCount ?: 0}",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                        )
                        Text(
                            text = "Total Sessions",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                            )
                        )
                    }
                }
            }
        }

        // Historical / Weekly focus indicators
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Weekly stats
                Card(
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, themeColors.glassBorder),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "${stats?.weeklyFocusMinutes ?: 0}m",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                        )
                        Text(
                            text = "Weekly Focus",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                            )
                        )
                    }
                }

                // Best streak
                Card(
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, themeColors.glassBorder),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "${stats?.bestStreak ?: 0} days",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = flameColor
                            )
                        )
                        Text(
                            text = "Best Streak",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                            )
                        )
                    }
                }
            }
        }

        // Recent Sessions Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent History",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                )
                
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = "History",
                    tint = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8)
                )
            }
        }

        // Sessions list items (safely fallback if history is empty)
        val sessions = stats?.recentSessions ?: emptyList()
        if (sessions.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = cardBg.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Info",
                            tint = if (isDark) Color(0xFF475569) else Color(0xFFCBD5E1),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No focus sessions recorded yet.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                                textAlign = TextAlign.Center
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Complete your first session to see stats here!",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8),
                                textAlign = TextAlign.Center
                            )
                        )
                    }
                }
            }
        } else {
            items(sessions) { session ->
                SessionHistoryItem(session = session, isDark = isDark, textColor = textColor, cardBg = cardBg)
            }
        }

        // Bottom spacer to compensate navigation bars
        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}
}

@Composable
fun SessionHistoryItem(
    session: SessionEntity,
    isDark: Boolean,
    textColor: Color,
    cardBg: Color
) {
    val themeColors = GlassThemeColors(isDark)
    val formatter = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
    val dateStr = formatter.format(Date(session.startTime))
    val focusMin = session.actualFocusSeconds / 60
    
    Card(
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, themeColors.glassBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Success vs failure icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            if (session.completed) Color(0xFF10B981).copy(alpha = 0.15f)
                            else Color(0xFFEF4444).copy(alpha = 0.15f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (session.completed) Icons.Default.CheckCircle else Icons.Default.Error,
                        contentDescription = "Status icon",
                        tint = if (session.completed) Color(0xFF10B981) else Color(0xFFEF4444),
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column {
                    Text(
                        text = session.presetName ?: "Custom Focus",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    )
                    Text(
                        text = dateStr,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                        )
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$focusMin m focus",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                )
                
                // Sound used badge if any
                if (session.soundUsed != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = "Sound Icon",
                            tint = themeColors.accentColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = session.soundUsed,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = themeColors.accentColor,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }
        }
    }
}
