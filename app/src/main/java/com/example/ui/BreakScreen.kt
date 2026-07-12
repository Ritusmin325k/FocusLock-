package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.FocusSessionService
import com.example.viewmodel.FocusViewModel
import com.example.ui.theme.GlassThemeColors
import com.example.ui.theme.LiquidGlassBackground

@Composable
fun BreakScreen(
    viewModel: FocusViewModel,
    sessionState: FocusSessionService.FocusSessionState,
    modifier: Modifier = Modifier
) {
    val isDark = viewModel.isDarkTheme()
    val themeColors = GlassThemeColors(isDark)
    val textColor = themeColors.textColor
    val accentColor = Color(0xFF10B981) // Keep Emerald for Break representation

    val totalSeconds = sessionState.breakDurationMinutes * 60
    val remainingSec = sessionState.remainingSeconds
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Break Icon
            Icon(
                imageVector = Icons.Default.LocalCafe,
                contentDescription = "Coffee break icon",
                tint = accentColor,
                modifier = Modifier
                    .size(80.dp)
                    .background(accentColor.copy(alpha = 0.15f), shape = CircleShape)
                    .border(1.dp, themeColors.glassBorder, CircleShape)
                    .padding(20.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Time to Recharge",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = textColor
                ),
                textAlign = TextAlign.Center
            )

            Text(
                text = "Stand up, stretch, or get some water.",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = themeColors.secondaryTextColor
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )

            // Progress indicator circle
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .padding(vertical = 40.dp)
                    .size(200.dp)
                    .clip(CircleShape)
                    .background(themeColors.cardBg)
                    .border(1.dp, themeColors.glassBorder, CircleShape)
            ) {
                CircularProgressIndicator(
                    progress = { 1.0f },
                    color = if (isDark) Color(0xFF374151) else Color(0xFFE5E7EB),
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
                    text = timeFormatted,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        fontSize = 44.sp
                    )
                )
            }

            // Action button to resume study early
            Button(
                onClick = { viewModel.endBreakEarly() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = themeColors.accentColor, // beautiful grey/silver theme button
                    contentColor = if (isDark) Color(0xFF111827) else Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .widthIn(max = 280.dp)
                    .testTag("end_break_early_button"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "End Break Early",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}
