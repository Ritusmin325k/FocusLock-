package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.PresetEntity
import com.example.data.SessionDatabase
import com.example.data.SessionEntity
import com.example.data.SessionRepository
import com.example.service.FocusSessionService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import android.os.Build

data class AppInfo(
    val packageName: String,
    val name: String
)

data class StatisticsSummary(
    val todayFocusMinutes: Int,
    val weeklyFocusMinutes: Int,
    val monthlyFocusMinutes: Int,
    val totalFocusHours: Float,
    val totalSessionsCount: Int,
    val currentStreak: Int,
    val bestStreak: Int,
    val averageSessionMinutes: Int,
    val longestSessionMinutes: Int,
    val calendarHeatmap: Map<String, Int>, // Date string ("yyyy-MM-dd") to minutes
    val recentSessions: List<SessionEntity>
)

class FocusViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SessionRepository
    private val sharedPrefs: SharedPreferences = application.getSharedPreferences("FocusLockPrefs", Context.MODE_PRIVATE)

    // State of the background service
    val serviceState: StateFlow<FocusSessionService.FocusSessionState?> = FocusSessionService.sessionState

    // Custom Saved Presets
    val customPresets: StateFlow<List<PresetEntity>>

    // Preloaded System Presets
    val systemPresets = listOf(
        PresetEntity(id = -1, name = "School Study", studyDurationMinutes = 25, breakDurationMinutes = 5, breaksCount = 3, appsToBlockJson = ""),
        PresetEntity(id = -2, name = "Homework", studyDurationMinutes = 30, breakDurationMinutes = 5, breaksCount = 2, appsToBlockJson = ""),
        PresetEntity(id = -3, name = "Reading", studyDurationMinutes = 15, breakDurationMinutes = 3, breaksCount = 1, appsToBlockJson = ""),
        PresetEntity(id = -4, name = "Coding", studyDurationMinutes = 50, breakDurationMinutes = 10, breaksCount = 4, appsToBlockJson = ""),
        PresetEntity(id = -5, name = "Exam Prep", studyDurationMinutes = 60, breakDurationMinutes = 15, breaksCount = 3, appsToBlockJson = "")
    )

    // Installed apps cache
    private val _installedApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val installedApps = _installedApps.asStateFlow()

    // Screen navigator state (simple State-driven view router)
    enum class AppScreen {
        DASHBOARD,
        CREATE_SESSION,
        FOCUS,
        BREAK,
        SUMMARY,
        STATISTICS,
        SETTINGS
    }
    
    private val _currentScreen = MutableStateFlow(AppScreen.DASHBOARD)
    val currentScreen = _currentScreen.asStateFlow()

    init {
        val dao = SessionDatabase.getDatabase(application).sessionDao()
        repository = SessionRepository(dao)
        
        customPresets = repository.allPresets.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Load installed apps asynchronously to prevent main-thread lag
        loadInstalledApps()
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    // Default settings
    fun getDefaultStudyDuration(): Int = sharedPrefs.getInt("default_study_duration", 25)
    fun getDefaultBreakDuration(): Int = sharedPrefs.getInt("default_break_duration", 5)
    fun getDefaultBreaksCount(): Int = sharedPrefs.getInt("default_breaks_count", 3)
    fun getDailyGoalMinutes(): Int = sharedPrefs.getInt("default_daily_goal", 60)
    fun isVibrationEnabled(): Boolean = sharedPrefs.getBoolean("vibration_enabled", true)
    fun isSoundLoopEnabled(): Boolean = sharedPrefs.getBoolean("sound_loop_enabled", true)
    fun isDarkTheme(): Boolean = sharedPrefs.getBoolean("dark_theme", true) // Default premium dark is true

    fun updateDefaultSettings(
        study: Int,
        breakDur: Int,
        breaks: Int,
        goal: Int,
        vibe: Boolean,
        loop: Boolean,
        dark: Boolean
    ) {
        sharedPrefs.edit().apply {
            putInt("default_study_duration", study)
            putInt("default_break_duration", breakDur)
            putInt("default_breaks_count", breaks)
            putInt("default_daily_goal", goal)
            putBoolean("vibration_enabled", vibe)
            putBoolean("sound_loop_enabled", loop)
            putBoolean("dark_theme", dark)
            apply()
        }
    }

    // Save Preset
    fun savePreset(name: String, study: Int, breakDur: Int, breaks: Int, blockedApps: List<String>) {
        viewModelScope.launch {
            val appsJson = blockedApps.joinToString(",")
            val preset = PresetEntity(
                name = name,
                studyDurationMinutes = study,
                breakDurationMinutes = breakDur,
                breaksCount = breaks,
                appsToBlockJson = appsJson
            )
            repository.insertPreset(preset)
        }
    }

    fun deletePreset(preset: PresetEntity) {
        if (preset.id >= 0) {
            viewModelScope.launch {
                repository.deletePresetById(preset.id)
            }
        }
    }

    // Load Apps to Block
    private fun loadInstalledApps() {
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>().applicationContext
            val pm = context.packageManager
            val intent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            
            val resolveInfos = pm.queryIntentActivities(intent, 0)
            val apps = resolveInfos.mapNotNull { info ->
                val pName = info.activityInfo.packageName
                // Filter out self
                if (pName == context.packageName) null else {
                    val appName = info.loadLabel(pm).toString()
                    AppInfo(packageName = pName, name = appName)
                }
            }.distinctBy { it.packageName }.sortedBy { it.name }

            _installedApps.value = apps
        }
    }

    // Active session controls interfacing with foreground service
    fun startSession(
        studyDuration: Int,
        breakDuration: Int,
        breaksCount: Int,
        blockedApps: List<String>,
        soundName: String?,
        volume: Float,
        pauseOnBreak: Boolean,
        presetName: String? = null
    ) {
        val context = getApplication<Application>().applicationContext
        val serviceIntent = Intent(context, FocusSessionService::class.java).apply {
            action = FocusSessionService.ACTION_START
            putExtra("study_duration", studyDuration)
            putExtra("break_duration", breakDuration)
            putExtra("breaks_count", breaksCount)
            putExtra("preset_name", presetName)
            putStringArrayListExtra("blocked_apps", ArrayList(blockedApps))
            putExtra("sound_name", soundName)
            putExtra("sound_volume", volume)
            putExtra("pause_music_breaks", pauseOnBreak)
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
        
        _currentScreen.value = AppScreen.FOCUS
    }

    fun pauseSession() {
        sendServiceAction(FocusSessionService.ACTION_PAUSE)
    }

    fun resumeSession() {
        sendServiceAction(FocusSessionService.ACTION_RESUME)
    }

    fun startBreak() {
        sendServiceAction(FocusSessionService.ACTION_START_BREAK)
        _currentScreen.value = AppScreen.BREAK
    }

    fun endBreakEarly() {
        sendServiceAction(FocusSessionService.ACTION_END_BREAK_EARLY)
        _currentScreen.value = AppScreen.FOCUS
    }

    fun stopSession() {
        sendServiceAction(FocusSessionService.ACTION_STOP)
        _currentScreen.value = AppScreen.SUMMARY
    }

    fun updateSessionSound(soundName: String, volume: Float) {
        val context = getApplication<Application>().applicationContext
        val intent = Intent(context, FocusSessionService::class.java).apply {
            action = FocusSessionService.ACTION_UPDATE_SOUND
            putExtra("sound_name", soundName)
            putExtra("sound_volume", volume)
        }
        context.startService(intent)
    }

    fun simulateBlockedAppInterruption(packageName: String) {
        val context = getApplication<Application>().applicationContext
        val intent = Intent(context, FocusSessionService::class.java).apply {
            action = FocusSessionService.ACTION_SIMULATE_BLOCK
            putExtra("package_name", packageName)
        }
        context.startService(intent)
    }

    private fun sendServiceAction(action: String) {
        val context = getApplication<Application>().applicationContext
        val serviceIntent = Intent(context, FocusSessionService::class.java).apply {
            this.action = action
        }
        context.startService(serviceIntent)
    }

    // Reactive Statistics Engine
    val statsSummary: StateFlow<StatisticsSummary?> = repository.allSessions.map { list ->
        if (list.isEmpty()) {
            return@map StatisticsSummary(
                todayFocusMinutes = 0,
                weeklyFocusMinutes = 0,
                monthlyFocusMinutes = 0,
                totalFocusHours = 0f,
                totalSessionsCount = 0,
                currentStreak = 0,
                bestStreak = 0,
                averageSessionMinutes = 0,
                longestSessionMinutes = 0,
                calendarHeatmap = emptyMap(),
                recentSessions = emptyList()
            )
        }

        val tz = TimeZone.getDefault()
        val nowCal = Calendar.getInstance(tz)
        val todayStart = Calendar.getInstance(tz).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val weekStart = todayStart - 6 * 24 * 60 * 60 * 1000L
        val monthStart = todayStart - 29 * 24 * 60 * 60 * 1000L

        var todaySum = 0
        var weekSum = 0
        var monthSum = 0
        var totalFocusSec = 0
        var totalSessions = 0
        var maxSessionSec = 0

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        sdf.timeZone = tz
        val heatmap = mutableMapOf<String, Int>()

        for (session in list) {
            if (session.completed || session.actualFocusSeconds > 10) {
                totalSessions++
                totalFocusSec += session.actualFocusSeconds
                if (session.actualFocusSeconds > maxSessionSec) {
                    maxSessionSec = session.actualFocusSeconds
                }

                val sessTime = session.startTime
                if (sessTime >= todayStart) {
                    todaySum += session.actualFocusSeconds
                }
                if (sessTime >= weekStart) {
                    weekSum += session.actualFocusSeconds
                }
                if (sessTime >= monthStart) {
                    monthSum += session.actualFocusSeconds
                }

                val dateKey = sdf.format(Date(sessTime))
                val existing = heatmap[dateKey] ?: 0
                heatmap[dateKey] = existing + (session.actualFocusSeconds / 60)
            }
        }

        // Calculate Streak based on active days (days with at least 1 min focus)
        val activeDays = list.filter { it.actualFocusSeconds >= 60 }
            .map { sdf.format(Date(it.startTime)) }
            .distinct()
            .sortedDescending() // newest to oldest

        var currentStreak = 0
        var bestStreak = 0

        if (activeDays.isNotEmpty()) {
            val todayStr = sdf.format(nowCal.time)
            nowCal.add(Calendar.DAY_OF_YEAR, -1)
            val yesterdayStr = sdf.format(nowCal.time)

            // Calculate current streak
            if (activeDays.contains(todayStr) || activeDays.contains(yesterdayStr)) {
                var checkingCal = Calendar.getInstance(tz)
                var checkingStr = sdf.format(checkingCal.time)
                if (!activeDays.contains(checkingStr)) {
                    checkingCal.add(Calendar.DAY_OF_YEAR, -1)
                    checkingStr = sdf.format(checkingCal.time)
                }

                while (activeDays.contains(checkingStr)) {
                    currentStreak++
                    checkingCal.add(Calendar.DAY_OF_YEAR, -1)
                    checkingStr = sdf.format(checkingCal.time)
                }
            }

            // Calculate best streak
            val sortedAsc = activeDays.map { sdf.parse(it)!! }.sorted() // oldest to newest
            if (sortedAsc.isNotEmpty()) {
                var tempStreak = 1
                bestStreak = 1
                
                for (idx in 1 until sortedAsc.size) {
                    val diff = sortedAsc[idx].time - sortedAsc[idx-1].time
                    val diffDays = diff / (24 * 60 * 60 * 1000)
                    if (diffDays == 1L) {
                        tempStreak++
                    } else if (diffDays > 1L) {
                        if (tempStreak > bestStreak) {
                            bestStreak = tempStreak
                        }
                        tempStreak = 1
                    }
                }
                if (tempStreak > bestStreak) {
                    bestStreak = tempStreak
                }
            }
        }

        val totalHours = totalFocusSec / 3600f
        val avgSessionSec = if (totalSessions > 0) totalFocusSec / totalSessions else 0

        StatisticsSummary(
            todayFocusMinutes = todaySum / 60,
            weeklyFocusMinutes = weekSum / 60,
            monthlyFocusMinutes = monthSum / 60,
            totalFocusHours = totalHours,
            totalSessionsCount = totalSessions,
            currentStreak = currentStreak,
            bestStreak = bestStreak,
            averageSessionMinutes = avgSessionSec / 60,
            longestSessionMinutes = maxSessionSec / 60,
            calendarHeatmap = heatmap,
            recentSessions = list.take(15)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )
}
