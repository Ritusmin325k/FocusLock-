package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PresetEntity
import com.example.viewmodel.AppInfo
import com.example.viewmodel.FocusViewModel
import com.example.ui.theme.GlassThemeColors
import com.example.ui.theme.LiquidGlassBackground
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import com.example.R
import android.app.AppOpsManager
import android.provider.Settings
import android.net.Uri
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateSessionScreen(
    viewModel: FocusViewModel,
    modifier: Modifier = Modifier
) {
    val isDark = viewModel.isDarkTheme()
    val themeColors = GlassThemeColors(isDark)
    val textColor = themeColors.textColor
    val cardBg = themeColors.cardBg
    val accentColor = themeColors.accentColor
    val context = androidx.compose.ui.platform.LocalContext.current
    var showPermissionExplanationDialog by remember { mutableStateOf(false) }

    // Form states
    var studyMin by remember { mutableStateOf(viewModel.getDefaultStudyDuration().toFloat()) }
    var breakMin by remember { mutableStateOf(viewModel.getDefaultBreakDuration().toFloat()) }
    var breaksCount by remember { mutableStateOf(viewModel.getDefaultBreaksCount().toFloat()) }
    
    // Focus Sound states
    val focusSounds = listOf("None", "Rain", "Forest", "Ocean", "Wind", "Fireplace", "Piano", "Binaural Beats", "Isochronic Tones", "White Noise", "Brown Noise", "Pink Noise")
    var selectedSound by remember { mutableStateOf("None") }
    var musicVolume by remember { mutableStateOf(0.5f) }
    var pauseMusicBreaks by remember { mutableStateOf(false) }

    // Preset Selection / Presets List
    val customPresetsList by viewModel.customPresets.collectAsState()
    var selectedPresetName by remember { mutableStateOf<String?>(null) }
    
    // Custom Preset Save state
    var showSavePresetDialog by remember { mutableStateOf(false) }
    var newPresetName by remember { mutableStateOf("") }

    // App blocking states
    val installedApps by viewModel.installedApps.collectAsState()
    var appSearchQuery by remember { mutableStateOf("") }
    var selectedApps by remember { mutableStateOf(setOf<String>()) }
    var isAppsSectionExpanded by remember { mutableStateOf(false) }

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
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
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
                        text = "Configure Session",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = textColor,
                            letterSpacing = 0.5.sp,
                            fontSize = 28.sp
                        )
                    )
                    Text(
                        text = "Set study and break targets custom fit for you.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                        )
                    )
                }
            }
        }

        // Favorites Presets Section
        item {
            Column {
                Text(
                    text = "Favorite Presets",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    ),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // System default presets
                    viewModel.systemPresets.forEach { preset ->
                        PresetChip(
                            preset = preset,
                            isSelected = selectedPresetName == preset.name,
                            isDark = isDark,
                            onClick = {
                                selectedPresetName = preset.name
                                studyMin = preset.studyDurationMinutes.toFloat()
                                breakMin = preset.breakDurationMinutes.toFloat()
                                breaksCount = preset.breaksCount.toFloat()
                            }
                        )
                    }

                    // Custom saved presets
                    customPresetsList.forEach { preset ->
                        PresetChip(
                            preset = preset,
                            isSelected = selectedPresetName == preset.name,
                            isDark = isDark,
                            canDelete = true,
                            onDelete = { viewModel.deletePreset(preset) },
                            onClick = {
                                selectedPresetName = preset.name
                                studyMin = preset.studyDurationMinutes.toFloat()
                                breakMin = preset.breakDurationMinutes.toFloat()
                                breaksCount = preset.breaksCount.toFloat()
                                if (preset.appsToBlockJson.isNotEmpty()) {
                                    selectedApps = preset.appsToBlockJson.split(",").toSet()
                                }
                            }
                        )
                    }
                }
            }
        }

        // Customizers (Study Duration, Break Duration, Breaks Count)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, themeColors.glassBorder),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // STUDY DURATION SLIDER
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Study Duration",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                        )
                        Text(
                            text = "${studyMin.toInt()} minutes",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = accentColor
                            )
                        )
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        IconButton(
                            onClick = { studyMin = (studyMin - 5).coerceAtLeast(5f) },
                            modifier = Modifier.background(if (isDark) Color(0xFF334155) else Color(0xFFF1F5F9), shape = CircleShape)
                        ) {
                            Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease", tint = textColor)
                        }
                        
                        Slider(
                            value = studyMin,
                            onValueChange = { 
                                studyMin = it
                                selectedPresetName = null // unset preset since values changed
                            },
                            valueRange = 5f..120f,
                            steps = 22, // 5 min intervals
                            colors = SliderDefaults.colors(
                                activeTrackColor = accentColor,
                                inactiveTrackColor = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0),
                                thumbColor = accentColor
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 12.dp)
                        )

                        IconButton(
                            onClick = { studyMin = (studyMin + 5).coerceIn(5f, 120f) },
                            modifier = Modifier.background(if (isDark) Color(0xFF334155) else Color(0xFFF1F5F9), shape = CircleShape)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Increase", tint = textColor)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = if (isDark) Color(0xFF334155) else Color(0xFFF1F5F9))
                    Spacer(modifier = Modifier.height(16.dp))

                    // BREAK DURATION SLIDER
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Break Duration",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                        )
                        Text(
                            text = "${breakMin.toInt()} minutes",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF10B981)
                            )
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        IconButton(
                            onClick = { breakMin = (breakMin - 1).coerceAtLeast(1f) },
                            modifier = Modifier.background(if (isDark) Color(0xFF334155) else Color(0xFFF1F5F9), shape = CircleShape)
                        ) {
                            Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease", tint = textColor)
                        }

                        Slider(
                            value = breakMin,
                            onValueChange = { 
                                breakMin = it
                                selectedPresetName = null
                            },
                            valueRange = 1f..30f,
                            steps = 28,
                            colors = SliderDefaults.colors(
                                activeTrackColor = Color(0xFF10B981),
                                inactiveTrackColor = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0),
                                thumbColor = Color(0xFF10B981)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 12.dp)
                        )

                        IconButton(
                            onClick = { breakMin = (breakMin + 1).coerceIn(1f, 30f) },
                            modifier = Modifier.background(if (isDark) Color(0xFF334155) else Color(0xFFF1F5F9), shape = CircleShape)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Increase", tint = textColor)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = if (isDark) Color(0xFF334155) else Color(0xFFF1F5F9))
                    Spacer(modifier = Modifier.height(16.dp))

                    // NUMBER OF BREAKS SLIDER
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Configured Breaks Count",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                        )
                        Text(
                            text = "${breaksCount.toInt()} breaks",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFF59E0B)
                            )
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        IconButton(
                            onClick = { breaksCount = (breaksCount - 1).coerceAtLeast(0f) },
                            modifier = Modifier.background(if (isDark) Color(0xFF334155) else Color(0xFFF1F5F9), shape = CircleShape)
                        ) {
                            Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease", tint = textColor)
                        }

                        Slider(
                            value = breaksCount,
                            onValueChange = { 
                                breaksCount = it
                                selectedPresetName = null
                            },
                            valueRange = 0f..10f,
                            steps = 9,
                            colors = SliderDefaults.colors(
                                activeTrackColor = Color(0xFFF59E0B),
                                inactiveTrackColor = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0),
                                thumbColor = Color(0xFFF59E0B)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 12.dp)
                        )

                        IconButton(
                            onClick = { breaksCount = (breaksCount + 1).coerceIn(0f, 10f) },
                            modifier = Modifier.background(if (isDark) Color(0xFF334155) else Color(0xFFF1F5F9), shape = CircleShape)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Increase", tint = textColor)
                        }
                    }
                }
            }
        }

        // Focus Sounds section (Music Synthesizer options)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, themeColors.glassBorder),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Focus Sound (Offline Synth)",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Dropdown simulation / selection row
                    var isDropdownExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = isDropdownExpanded,
                        onExpandedChange = { isDropdownExpanded = !isDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedSound,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Select Sound Type") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = textColor,
                                unfocusedTextColor = textColor,
                                focusedBorderColor = accentColor,
                                unfocusedBorderColor = if (isDark) Color(0xFF475569) else Color(0xFFCBD5E1),
                                focusedLabelColor = accentColor,
                                unfocusedLabelColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                            ),
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = isDropdownExpanded,
                            onDismissRequest = { isDropdownExpanded = false },
                            modifier = Modifier.background(cardBg)
                        ) {
                            focusSounds.forEach { sound ->
                                DropdownMenuItem(
                                    text = { Text(sound, color = textColor) },
                                    onClick = {
                                        selectedSound = sound
                                        isDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Sound volume adjustments
                    if (selectedSound != "None") {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Synthesizer Volume",
                                style = MaterialTheme.typography.bodyMedium.copy(color = textColor)
                            )
                            Text(
                                text = "${(musicVolume * 100).toInt()}%",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = accentColor)
                            )
                        }
                        Slider(
                            value = musicVolume,
                            onValueChange = { musicVolume = it },
                            colors = SliderDefaults.colors(activeTrackColor = accentColor, thumbColor = accentColor)
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Pause music during breaks",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = textColor, fontWeight = FontWeight.SemiBold)
                                )
                                Text(
                                    text = "Silence focus sounds automatically when relaxation breaks are active.",
                                    style = MaterialTheme.typography.bodySmall.copy(color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B))
                                )
                            }
                            Switch(
                                checked = pauseMusicBreaks,
                                onCheckedChange = { pauseMusicBreaks = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = accentColor,
                                    checkedTrackColor = accentColor.copy(alpha = 0.5f)
                                )
                            )
                        }
                    }
                }
            }
        }

        // App Blocking Section (Collapsible checklist with search bar)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, themeColors.glassBorder),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isAppsSectionExpanded = !isAppsSectionExpanded },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Block Distracting Apps",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = textColor
                                )
                            )
                            Text(
                                text = "${selectedApps.size} apps configured for locking.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                                )
                            )
                        }
                        
                        TextButton(onClick = { isAppsSectionExpanded = !isAppsSectionExpanded }) {
                            Text(if (isAppsSectionExpanded) "Collapse" else "Expand", color = accentColor)
                        }
                    }

                    AnimatedVisibility(visible = isAppsSectionExpanded) {
                        Column {
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // App search bar
                            OutlinedTextField(
                                value = appSearchQuery,
                                onValueChange = { appSearchQuery = it },
                                label = { Text("Search installed apps") },
                                trailingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = textColor) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = textColor,
                                    unfocusedTextColor = textColor,
                                    focusedBorderColor = accentColor,
                                    unfocusedBorderColor = if (isDark) Color(0xFF475569) else Color(0xFFCBD5E1)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Filter apps based on query
                            val filteredApps = installedApps.filter {
                                it.name.contains(appSearchQuery, ignoreCase = true) ||
                                it.packageName.contains(appSearchQuery, ignoreCase = true)
                            }

                            if (filteredApps.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No apps found. Try searching for something else.",
                                        color = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8),
                                        fontSize = 13.sp
                                    )
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 240.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    items(filteredApps) { app ->
                                        val isChecked = selectedApps.contains(app.packageName)
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable {
                                                    selectedApps = if (isChecked) {
                                                        selectedApps - app.packageName
                                                    } else {
                                                        selectedApps + app.packageName
                                                    }
                                                }
                                                .padding(vertical = 4.dp, horizontal = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(text = app.name, color = textColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                Text(text = app.packageName, color = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8), fontSize = 11.sp)
                                            }
                                            Checkbox(
                                                checked = isChecked,
                                                onCheckedChange = {
                                                    selectedApps = if (isChecked) {
                                                        selectedApps - app.packageName
                                                    } else {
                                                        selectedApps + app.packageName
                                                    }
                                                },
                                                colors = CheckboxDefaults.colors(checkedColor = accentColor)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Save Custom Preset Row / Trigger
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { showSavePresetDialog = true },
                    modifier = Modifier.testTag("save_as_preset_button")
                ) {
                    Icon(imageVector = Icons.Default.BookmarkBorder, contentDescription = "Preset icon")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Save configuration as Favorite Preset", color = accentColor)
                }
            }
        }

        // START TRIGGER
        item {
            Button(
                onClick = {
                    if (selectedApps.isNotEmpty()) {
                        val usageOk = hasUsageStatsPermission(context)
                        val overlayOk = hasOverlayPermission(context)
                        if (!usageOk || !overlayOk) {
                            showPermissionExplanationDialog = true
                            return@Button
                        }
                    }
                    viewModel.startSession(
                        studyDuration = studyMin.toInt(),
                        breakDuration = breakMin.toInt(),
                        breaksCount = breaksCount.toInt(),
                        blockedApps = selectedApps.toList(),
                        soundName = if (selectedSound == "None") null else selectedSound,
                        volume = musicVolume,
                        pauseOnBreak = pauseMusicBreaks,
                        presetName = selectedPresetName ?: "Custom Focus"
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("start_session_button")
            ) {
                Text(
                    text = "Launch Focus Session",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
    }

    // Save Preset Dialog
    if (showSavePresetDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showSavePresetDialog = false },
            title = { Text("Save Favorite Preset", color = textColor) },
            text = {
                Column {
                    Text("Enter a unique name to save these current focus and break configurations.", color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B), modifier = Modifier.padding(bottom = 8.dp))
                    OutlinedTextField(
                        value = newPresetName,
                        onValueChange = { newPresetName = it },
                        singleLine = true,
                        placeholder = { Text("e.g., Deep Coding") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = textColor, unfocusedTextColor = textColor, focusedBorderColor = accentColor, unfocusedBorderColor = Color.Gray),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newPresetName.isNotBlank()) {
                            viewModel.savePreset(
                                name = newPresetName,
                                study = studyMin.toInt(),
                                breakDur = breakMin.toInt(),
                                breaks = breaksCount.toInt(),
                                blockedApps = selectedApps.toList()
                            )
                            selectedPresetName = newPresetName
                            newPresetName = ""
                            showSavePresetDialog = false
                        }
                    }
                ) {
                    Text("Save", color = accentColor, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSavePresetDialog = false }) {
                    Text("Cancel", color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B))
                }
            },
            containerColor = cardBg
        )
    }

    // Permission Explanation Dialog
    if (showPermissionExplanationDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showPermissionExplanationDialog = false },
            title = { Text("Permissions Required", color = textColor, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "To block other apps during your focus session, FocusLock needs system permissions:",
                        color = textColor,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    // Usage Stats Explanation
                    val hasUsage = hasUsageStatsPermission(context)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (hasUsage) Icons.Default.CheckCircle else Icons.Default.Info,
                            contentDescription = null,
                            tint = if (hasUsage) Color(0xFF10B981) else Color(0xFFF59E0B)
                        )
                        Column {
                            Text("Usage Access", fontWeight = FontWeight.Bold, color = textColor)
                            Text(
                                "Detects when a blocked app is opened.",
                                color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    // Overlay Explanation
                    val hasOverlay = hasOverlayPermission(context)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (hasOverlay) Icons.Default.CheckCircle else Icons.Default.Info,
                            contentDescription = null,
                            tint = if (hasOverlay) Color(0xFF10B981) else Color(0xFFF59E0B)
                        )
                        Column {
                            Text("Draw over other apps", fontWeight = FontWeight.Bold, color = textColor)
                            Text(
                                "Displays the blocking screen on top of other apps.",
                                color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    val hasUsage = hasUsageStatsPermission(context)
                    val hasOverlay = hasOverlayPermission(context)

                    if (!hasUsage) {
                        Button(
                            onClick = {
                                try {
                                    val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    // fallback
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                        ) {
                            Text("Grant Usage Access", color = Color.White)
                        }
                    }

                    if (!hasOverlay) {
                        Button(
                            onClick = {
                                try {
                                    val intent = Intent(
                                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                        Uri.parse("package:${context.packageName}")
                                    ).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    try {
                                        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                        context.startActivity(intent)
                                    } catch (ex: Exception) {}
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                        ) {
                            Text("Grant Draw Over Apps", color = Color.White)
                        }
                    }

                    if (hasUsage && hasOverlay) {
                        Button(
                            onClick = {
                                showPermissionExplanationDialog = false
                                viewModel.startSession(
                                    studyDuration = studyMin.toInt(),
                                    breakDuration = breakMin.toInt(),
                                    breaksCount = breaksCount.toInt(),
                                    blockedApps = selectedApps.toList(),
                                    soundName = if (selectedSound == "None") null else selectedSound,
                                    volume = musicVolume,
                                    pauseOnBreak = pauseMusicBreaks,
                                    presetName = selectedPresetName ?: "Custom Focus"
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                        ) {
                            Text("Start Focus Session", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionExplanationDialog = false }) {
                    Text("Cancel", color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B))
                }
            },
            containerColor = cardBg
        )
    }
}

@Composable
fun PresetChip(
    preset: PresetEntity,
    isSelected: Boolean,
    isDark: Boolean,
    canDelete: Boolean = false,
    onDelete: () -> Unit = {},
    onClick: () -> Unit
) {
    val themeColors = GlassThemeColors(isDark)
    val chipColor = if (isSelected) themeColors.accentColor else if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
    val textAndIconColor = if (isSelected) {
        if (isDark) Color(0xFF111827) else Color.White
    } else {
        if (isDark) Color.White else Color(0xFF1E293B)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(chipColor)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Icon(
            imageVector = if (isSelected) Icons.Default.Favorite else Icons.Default.Bookmark,
            contentDescription = "Bookmark",
            tint = textAndIconColor,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = preset.name,
            color = textAndIconColor,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
        if (canDelete) {
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Delete Preset",
                tint = textAndIconColor.copy(alpha = 0.7f),
                modifier = Modifier
                    .size(14.dp)
                    .clickable { onDelete() }
            )
        }
    }
}

private fun hasUsageStatsPermission(context: Context): Boolean {
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager ?: return false
    val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        appOps.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            context.packageName
        )
    } else {
        @Suppress("DEPRECATION")
        appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            context.packageName
        )
    }
    return mode == AppOpsManager.MODE_ALLOWED
}

private fun hasOverlayPermission(context: Context): Boolean {
    return Settings.canDrawOverlays(context)
}
