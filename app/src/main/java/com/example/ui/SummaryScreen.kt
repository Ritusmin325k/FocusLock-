package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.service.FocusSessionService
import com.example.viewmodel.FocusViewModel
import com.example.ui.theme.GlassThemeColors
import com.example.ui.theme.LiquidGlassBackground

@Composable
fun SummaryScreen(
    viewModel: FocusViewModel,
    sessionState: FocusSessionService.FocusSessionState?,
    onBackToDashboard: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = viewModel.isDarkTheme()
    val themeColors = GlassThemeColors(isDark)
    val textColor = themeColors.textColor
    val cardBg = themeColors.cardBg
    val accentColor = themeColors.accentColor
    val successColor = Color(0xFF10B981) // Green

    val wasCompleted = sessionState?.currentPhase == FocusSessionService.FocusPhase.COMPLETED

    LiquidGlassBackground(
        isDark = isDark,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
        // Visual Celebration Ring
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding(bottom = 24.dp)
                .size(120.dp)
                .background(
                    if (wasCompleted) successColor.copy(alpha = 0.15f)
                    else Color(0xFFEF4444).copy(alpha = 0.15f),
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = if (wasCompleted) Icons.Default.CheckCircle else Icons.Default.Star,
                contentDescription = "Session state icon",
                tint = if (wasCompleted) successColor else accentColor,
                modifier = Modifier.size(60.dp)
            )
        }

        Text(
            text = if (wasCompleted) "Congratulations!" else "Session Completed",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                color = textColor
            ),
            textAlign = TextAlign.Center
        )

        Text(
            text = if (wasCompleted) "You crushed your focus goals today!" else "Progress is a marathon, not a sprint. Great effort!",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(top = 8.dp, bottom = 24.dp)
                .widthIn(max = 300.dp)
        )

        // Statistics Summary Card Grid
        Card(
            colors = CardDefaults.cardColors(containerColor = cardBg),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Session Stats",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                )

                // Row 1: Focus & Break Times
                Row(modifier = Modifier.fillMaxWidth()) {
                    SummaryStatItem(
                        icon = Icons.Default.Timer,
                        label = "Focus Time",
                        value = formatSecondsToMinutes(sessionState?.totalFocusSecondsSpent ?: 0),
                        isDark = isDark,
                        textColor = textColor,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryStatItem(
                        icon = Icons.Default.Coffee,
                        label = "Break Time",
                        value = formatSecondsToMinutes(sessionState?.totalBreakSecondsSpent ?: 0),
                        isDark = isDark,
                        textColor = textColor,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Row 2: Breaks Used & Blocked Apps
                Row(modifier = Modifier.fillMaxWidth()) {
                    SummaryStatItem(
                        icon = Icons.Default.Done,
                        label = "Breaks Used",
                        value = "${sessionState?.breaksUsed ?: 0} / ${sessionState?.totalBreaksConfigured ?: 0}",
                        isDark = isDark,
                        textColor = textColor,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryStatItem(
                        icon = Icons.Default.Block,
                        label = "Apps Locked",
                        value = "${sessionState?.blockedApps?.size ?: 0} apps",
                        isDark = isDark,
                        textColor = textColor,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Row 3: Focus Sound & Streak
                Row(modifier = Modifier.fillMaxWidth()) {
                    SummaryStatItem(
                        icon = Icons.Default.MusicNote,
                        label = "Sound Used",
                        value = sessionState?.selectedSound ?: "Silent Mode",
                        isDark = isDark,
                        textColor = textColor,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryStatItem(
                        icon = Icons.Default.Timeline,
                        label = "Current Streak",
                        value = "Active", // Streak is updated in statistics
                        isDark = isDark,
                        textColor = textColor,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Back to Dashboard Button
        Button(
            onClick = onBackToDashboard,
            colors = ButtonDefaults.buttonColors(
                containerColor = accentColor,
                contentColor = if (isDark) Color(0xFF111827) else Color.White
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .widthIn(max = 280.dp)
                .testTag("summary_back_to_dashboard_button")
        ) {
            Text(
                text = "Back to Dashboard",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
    }
}

@Composable
fun SummaryStatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    isDark: Boolean,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    val themeColors = GlassThemeColors(isDark)
    Row(
        modifier = modifier.padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = themeColors.accentColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = value,
                color = textColor,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Text(
                text = label,
                color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                fontSize = 11.sp
            )
        }
    }
}

private fun formatSecondsToMinutes(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return if (m > 0) "${m}m ${s}s" else "${s}s"
}
