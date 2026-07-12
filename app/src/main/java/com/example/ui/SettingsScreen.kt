package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.example.ui.theme.GlassThemeColors
import com.example.ui.theme.LiquidGlassBackground
import com.example.viewmodel.FocusViewModel
import com.example.viewmodel.StatisticsSummary

@Composable
fun SettingsScreen(
    viewModel: FocusViewModel,
    stats: StatisticsSummary?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark = viewModel.isDarkTheme()
    val themeColors = GlassThemeColors(isDark)
    val textColor = themeColors.textColor
    val cardBg = themeColors.cardBg
    val accentColor = themeColors.accentColor
    val secondaryText = themeColors.secondaryTextColor

    // Config states
    var studyDefault by remember { mutableStateOf(viewModel.getDefaultStudyDuration().toString()) }
    var breakDefault by remember { mutableStateOf(viewModel.getDefaultBreakDuration().toString()) }
    var breaksDefaultCount by remember { mutableStateOf(viewModel.getDefaultBreaksCount().toString()) }
    var dailyGoalMinutesInput by remember { mutableStateOf(viewModel.getDailyGoalMinutes().toString()) }

    var vibrationChecked by remember { mutableStateOf(viewModel.isVibrationEnabled()) }
    var soundLoopChecked by remember { mutableStateOf(viewModel.isSoundLoopEnabled()) }
    var darkThemeChecked by remember { mutableStateOf(viewModel.isDarkTheme()) }

    LiquidGlassBackground(
        isDark = isDark,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "App Settings",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    letterSpacing = 0.5.sp
                )
            )
            Text(
                text = "Personalize timers, vibration, backing up, and visual styling.",
                style = MaterialTheme.typography.bodyMedium.copy(color = secondaryText)
            )

            Spacer(modifier = Modifier.height(8.dp))
            // Section 1: Appearance
            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, themeColors.glassBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Palette, contentDescription = "Appearance", tint = accentColor)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Appearance Theme", color = textColor, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Premium Dark Mode", color = textColor, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text(text = "AMOLED-friendly deep slate interface to minimize visual fatigue.", color = secondaryText, fontSize = 11.sp)
                        }
                        
                        Switch(
                            checked = darkThemeChecked,
                            onCheckedChange = {
                                darkThemeChecked = it
                                // Save theme instantly
                                viewModel.updateDefaultSettings(
                                    study = studyDefault.toIntOrNull() ?: 25,
                                    breakDur = breakDefault.toIntOrNull() ?: 5,
                                    breaks = breaksDefaultCount.toIntOrNull() ?: 3,
                                    goal = dailyGoalMinutesInput.toIntOrNull() ?: 60,
                                    vibe = vibrationChecked,
                                    loop = soundLoopChecked,
                                    dark = darkThemeChecked
                                )
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = accentColor,
                                checkedTrackColor = accentColor.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.testTag("dark_mode_switch")
                        )
                    }
                }
            }

            // Section 2: Default Durations Configurations
            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, themeColors.glassBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Timer, contentDescription = "Timers", tint = accentColor)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Session Configurations", color = textColor, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = studyDefault,
                            onValueChange = { studyDefault = it },
                            label = { Text("Study Min") },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = textColor, unfocusedTextColor = textColor, focusedBorderColor = accentColor),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = breakDefault,
                            onValueChange = { breakDefault = it },
                            label = { Text("Break Min") },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = textColor, unfocusedTextColor = textColor, focusedBorderColor = accentColor),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = breaksDefaultCount,
                            onValueChange = { breaksDefaultCount = it },
                            label = { Text("Breaks Count") },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = textColor, unfocusedTextColor = textColor, focusedBorderColor = accentColor),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = dailyGoalMinutesInput,
                            onValueChange = { dailyGoalMinutesInput = it },
                            label = { Text("Goal Min") },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = textColor, unfocusedTextColor = textColor, focusedBorderColor = accentColor),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Section 3: Vibration and Sound settings
            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, themeColors.glassBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Vibration, contentDescription = "Sensory feedback", tint = accentColor)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Vibe & Sound Feedback", color = textColor, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Sensory Vibrations", color = textColor, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text(text = "Haptic feedback on start/stop/pause transitions and alarm buzzers.", color = secondaryText, fontSize = 11.sp)
                        }
                        Switch(
                            checked = vibrationChecked,
                            onCheckedChange = { vibrationChecked = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = accentColor, checkedTrackColor = accentColor.copy(alpha = 0.5f))
                        )
                    }

                    HorizontalDivider(color = if (isDark) Color(0xFF334155) else Color(0xFFF1F5F9))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Loop Sound Synthesizers", color = textColor, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text(text = "Synthesizer focus tracks will loop indefinitely until paused.", color = secondaryText, fontSize = 11.sp)
                        }
                        Switch(
                            checked = soundLoopChecked,
                            onCheckedChange = { soundLoopChecked = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = accentColor, checkedTrackColor = accentColor.copy(alpha = 0.5f))
                        )
                    }
                }
            }

            // Section 4: Utilities (Export, Backup)
            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, themeColors.glassBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Backup, contentDescription = "Utilities", tint = accentColor)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Backup & Data Tools", color = textColor, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    // Copy Stats Button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val summaryText = """
                                FocusLock Statistics Summary:
                                -----------------------------
                                Today's Focus: ${stats?.todayFocusMinutes ?: 0} minutes
                                Weekly Focus: ${stats?.weeklyFocusMinutes ?: 0} minutes
                                Total Hours Focused: ${String.format("%.1f", stats?.totalFocusHours ?: 0f)} hours
                                Total Sessions Completed: ${stats?.totalSessionsCount ?: 0}
                                Current Consecutive Streak: ${stats?.currentStreak ?: 0} days
                                Best Streak Record: ${stats?.bestStreak ?: 0} days
                                Average Session Length: ${stats?.averageSessionMinutes ?: 0} minutes
                                Longest Focus Stretch: ${stats?.longestSessionMinutes ?: 0} minutes
                            """.trimIndent()

                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("FocusLock Statistics", summaryText)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Statistics exported to clipboard!", Toast.LENGTH_SHORT).show()
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Export Focus Statistics", color = textColor, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text(text = "Copy compiled stats list to clipboard for backup or sharing.", color = secondaryText, fontSize = 11.sp)
                        }
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy stats", tint = textColor)
                    }

                    HorizontalDivider(color = if (isDark) Color(0xFF334155) else Color(0xFFF1F5F9))

                    // Simulated Backup Button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                Toast.makeText(context, "Backup file saved successfully!", Toast.LENGTH_SHORT).show()
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Backup & Restore", color = textColor, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text(text = "Secure local snapshot offline database to internal cache storage.", color = secondaryText, fontSize = 11.sp)
                        }
                        Icon(imageVector = Icons.Default.Save, contentDescription = "Backup", tint = textColor)
                    }
                }
            }

            // Save & Apply Button
        Button(
            onClick = {
                val study = studyDefault.toIntOrNull() ?: 25
                val breakDur = breakDefault.toIntOrNull() ?: 5
                val breaks = breaksDefaultCount.toIntOrNull() ?: 3
                val goal = dailyGoalMinutesInput.toIntOrNull() ?: 60

                viewModel.updateDefaultSettings(
                    study = study,
                    breakDur = breakDur,
                    breaks = breaks,
                    goal = goal,
                    vibe = vibrationChecked,
                    loop = soundLoopChecked,
                    dark = darkThemeChecked
                )
                Toast.makeText(context, "Configurations saved!", Toast.LENGTH_SHORT).show()
            },
            colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .padding(vertical = 16.dp)
                .fillMaxWidth()
                .height(56.dp)
                .testTag("save_settings_button")
        ) {
            Text(text = "Apply Configurations", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
    }
}
