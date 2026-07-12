package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.SessionDatabase
import com.example.data.SessionEntity
import com.example.data.SessionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class FocusSessionService : Service() {

    enum class FocusPhase {
        STUDY,
        BREAK,
        COMPLETED
    }

    enum class FocusState {
        IDLE,
        RUNNING,
        PAUSED
    }

    data class FocusSessionState(
        val studyDurationMinutes: Int,
        val breakDurationMinutes: Int,
        val totalBreaksConfigured: Int,
        val breaksUsed: Int,
        val totalFocusSecondsSpent: Int,
        val totalBreakSecondsSpent: Int,
        val remainingSeconds: Int,
        val currentPhase: FocusPhase,
        val currentState: FocusState,
        val blockedApps: List<String>,
        val selectedSound: String?,
        val soundVolume: Float,
        val isSoundPlaying: Boolean,
        val pauseMusicDuringBreaks: Boolean,
        val startTime: Long,
        val presetName: String? = null
    )

    companion object {
        private const val CHANNEL_ID = "focus_lock_session_channel"
        private const val NOTIFICATION_ID = 9001

        const val ACTION_START = "com.example.focuslock.START"
        const val ACTION_PAUSE = "com.example.focuslock.PAUSE"
        const val ACTION_RESUME = "com.example.focuslock.RESUME"
        const val ACTION_START_BREAK = "com.example.focuslock.START_BREAK"
        const val ACTION_END_BREAK_EARLY = "com.example.focuslock.END_BREAK"
        const val ACTION_STOP = "com.example.focuslock.STOP"
        const val ACTION_UPDATE_SOUND = "com.example.focuslock.UPDATE_SOUND"
        const val ACTION_SIMULATE_BLOCK = "com.example.focuslock.SIMULATE_BLOCK"

        private val _sessionState = MutableStateFlow<FocusSessionState?>(null)
        val sessionState = _sessionState.asStateFlow()

        // Helper to check if service is running
        fun isRunning(): Boolean {
            return _sessionState.value != null && _sessionState.value?.currentState != FocusState.IDLE
        }
    }

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var timerJob: Job? = null
    private var databaseJob: Job? = null
    private val soundSynthesizer = SoundSynthesizer()

    // Configuration
    private var studyDurationMin = 25
    private var breakDurationMin = 5
    private var totalBreaksCount = 3
    private var breaksUsedCount = 0
    private var presetName: String? = null
    private var blockedAppsList = listOf<String>()
    
    // Internal Timers
    private var phase = FocusPhase.STUDY
    private var state = FocusState.IDLE
    private var secondsRemaining = 0
    private var totalFocusSeconds = 0
    private var totalBreakSeconds = 0
    private var sessionStartTime = 0L

    // Focus Music Config
    private var soundName: String? = null
    private var soundVolume = 0.5f
    private var soundPlaying = false
    private var pauseMusicOnBreaks = false

    private lateinit var repository: SessionRepository

    override fun onCreate() {
        super.onCreate()
        val db = SessionDatabase.getDatabase(applicationContext)
        repository = SessionRepository(db.sessionDao())
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: return START_STICKY

        when (action) {
            ACTION_START -> {
                studyDurationMin = intent.getIntExtra("study_duration", 25)
                breakDurationMin = intent.getIntExtra("break_duration", 5)
                totalBreaksCount = intent.getIntExtra("breaks_count", 3)
                presetName = intent.getStringExtra("preset_name")
                blockedAppsList = intent.getStringArrayListExtra("blocked_apps") ?: listOf()
                
                // Audio
                soundName = intent.getStringExtra("sound_name")
                soundVolume = intent.getFloatExtra("sound_volume", 0.5f)
                pauseMusicOnBreaks = intent.getBooleanExtra("pause_music_breaks", false)

                startFocusSession()
            }
            ACTION_PAUSE -> pauseSession()
            ACTION_RESUME -> resumeSession()
            ACTION_START_BREAK -> startBreak()
            ACTION_END_BREAK_EARLY -> endBreakEarly()
            ACTION_STOP -> stopFocusSession(completed = false)
            ACTION_UPDATE_SOUND -> {
                val newSound = intent.getStringExtra("sound_name")
                val newVol = intent.getFloatExtra("sound_volume", -1f)
                val newPause = intent.hasExtra("pause_music_breaks")
                
                if (newSound != null || intent.hasExtra("sound_name")) {
                    soundName = if (newSound == "None") null else newSound
                }
                if (newVol >= 0f) {
                    soundVolume = newVol
                }
                if (newPause) {
                    pauseMusicOnBreaks = intent.getBooleanExtra("pause_music_breaks", false)
                }

                updateAudioState()
                updateNotification()
                publishState()
            }
            ACTION_SIMULATE_BLOCK -> {
                // Instantly trigger blocking activity for simulation purposes
                val pkg = intent.getStringExtra("package_name") ?: "com.socialmedia.distraction"
                triggerBlockScreen(pkg)
            }
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startFocusSession() {
        phase = FocusPhase.STUDY
        state = FocusState.RUNNING
        secondsRemaining = studyDurationMin * 60
        breaksUsedCount = 0
        totalFocusSeconds = 0
        totalBreakSeconds = 0
        sessionStartTime = System.currentTimeMillis()

        // Start Foreground Service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                createNotification(),
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } else {
            startForeground(NOTIFICATION_ID, createNotification())
        }

        // Start Sound Synthesizer
        soundPlaying = soundName != null
        if (soundPlaying && soundName != "None") {
            soundSynthesizer.start(soundName!!, soundVolume)
        }

        // Start active timer loop
        startTimerLoop()
        publishState()
    }

    private fun pauseSession() {
        if (phase == FocusPhase.STUDY && state == FocusState.RUNNING) {
            state = FocusState.PAUSED
            if (soundPlaying) {
                soundSynthesizer.stop()
            }
            updateNotification()
            publishState()
        }
    }

    private fun resumeSession() {
        if (phase == FocusPhase.STUDY && state == FocusState.PAUSED) {
            state = FocusState.RUNNING
            if (soundPlaying && soundName != null) {
                soundSynthesizer.start(soundName!!, soundVolume)
            }
            updateNotification()
            publishState()
        }
    }

    private fun startBreak() {
        if (state == FocusState.RUNNING && phase == FocusPhase.STUDY && breaksUsedCount < totalBreaksCount) {
            phase = FocusPhase.BREAK
            secondsRemaining = breakDurationMin * 60
            breaksUsedCount++

            // Audio logic on break start
            if (pauseMusicOnBreaks) {
                soundSynthesizer.stop()
            } else if (soundPlaying && soundName != null) {
                soundSynthesizer.start(soundName!!, soundVolume)
            }

            playAlertAndVibrate()
            updateNotification()
            publishState()
        }
    }

    private fun endBreakEarly() {
        if (phase == FocusPhase.BREAK) {
            endBreakAndResumeStudy()
        }
    }

    private fun endBreakAndResumeStudy() {
        phase = FocusPhase.STUDY
        secondsRemaining = studyDurationMin * 60 // reset or continue? requirements: "resumes study timer automatically"

        // Restore Audio if it was paused
        if (pauseMusicOnBreaks && soundPlaying && soundName != null) {
            soundSynthesizer.start(soundName!!, soundVolume)
        }

        playAlertAndVibrate()
        updateNotification()
        publishState()
    }

    private fun updateAudioState() {
        if (soundName == null || soundName == "None") {
            soundSynthesizer.stop()
            soundPlaying = false
        } else {
            soundPlaying = true
            // If running, or (if on break and NOT pausing on break)
            val shouldPlay = state == FocusState.RUNNING && (phase == FocusPhase.STUDY || !pauseMusicOnBreaks)
            if (shouldPlay) {
                soundSynthesizer.start(soundName!!, soundVolume)
            } else {
                soundSynthesizer.stop()
            }
        }
    }

    private fun startTimerLoop() {
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            while (isActive) {
                delay(1000)
                if (state == FocusState.RUNNING) {
                    secondsRemaining--
                    
                    if (phase == FocusPhase.STUDY) {
                        totalFocusSeconds++
                        // App blocking check (Usage Stats)
                        checkBlockedApps()
                    } else {
                        totalBreakSeconds++
                    }

                    if (secondsRemaining <= 0) {
                        if (phase == FocusPhase.STUDY) {
                            // Focus timer completed!
                            stopFocusSession(completed = true)
                            break
                        } else {
                            // Break timer completed! Go back to study automatically
                            endBreakAndResumeStudy()
                        }
                    } else {
                        // Periodic update
                        if (secondsRemaining % 5 == 0) {
                            updateNotification()
                        }
                        publishState()
                    }
                }
            }
        }
    }

    private fun stopFocusSession(completed: Boolean) {
        state = FocusState.IDLE
        soundSynthesizer.stop()
        timerJob?.cancel()
        timerJob = null

        val finalState = FocusSessionState(
            studyDurationMinutes = studyDurationMin,
            breakDurationMinutes = breakDurationMin,
            totalBreaksConfigured = totalBreaksCount,
            breaksUsed = breaksUsedCount,
            totalFocusSecondsSpent = totalFocusSeconds,
            totalBreakSecondsSpent = totalBreakSeconds,
            remainingSeconds = 0,
            currentPhase = if (completed) FocusPhase.COMPLETED else phase,
            currentState = FocusState.IDLE,
            blockedApps = blockedAppsList,
            selectedSound = soundName,
            soundVolume = soundVolume,
            isSoundPlaying = false,
            pauseMusicDuringBreaks = pauseMusicOnBreaks,
            startTime = sessionStartTime,
            presetName = presetName
        )

        _sessionState.value = finalState

        // Save session to database
        databaseJob = serviceScope.launch(Dispatchers.IO) {
            val entity = SessionEntity(
                startTime = sessionStartTime,
                studyDurationMinutes = studyDurationMin,
                breakDurationMinutes = breakDurationMin,
                breaksConfigured = totalBreaksCount,
                breaksUsed = breaksUsedCount,
                actualFocusSeconds = totalFocusSeconds,
                actualBreakSeconds = totalBreakSeconds,
                presetName = presetName,
                appsBlockedCount = blockedAppsList.size,
                soundUsed = soundName,
                completed = completed
            )
            repository.insertSession(entity)
            
            // Stop Foreground and Service self
            launch(Dispatchers.Main) {
                playAlertAndVibrate()
                stopForeground(true)
                stopSelf()
            }
        }
    }

    private fun checkBlockedApps() {
        if (blockedAppsList.isEmpty()) return
        
        serviceScope.launch(Dispatchers.IO) {
            try {
                val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager ?: return@launch
                val endTime = System.currentTimeMillis()
                val startTime = endTime - 15000 // 15 seconds to be safe
                
                var foregroundPkg: String? = null
                
                // Approach 1: Query Events (most precise)
                val events = usageStatsManager.queryEvents(startTime, endTime)
                val event = UsageEvents.Event()
                while (events.hasNextEvent()) {
                    events.getNextEvent(event)
                    if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED || event.eventType == 1) {
                        foregroundPkg = event.packageName
                    }
                }
                
                // Approach 2: Query Usage Stats (highly reliable fallback)
                if (foregroundPkg == null) {
                    val stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime)
                    if (!stats.isNullOrEmpty()) {
                        val activeApp = stats.maxByOrNull { it.lastTimeUsed }
                        if (activeApp != null && (endTime - activeApp.lastTimeUsed) < 15000) {
                            foregroundPkg = activeApp.packageName
                        }
                    }
                }

                if (foregroundPkg != null && blockedAppsList.contains(foregroundPkg)) {
                    // Filter out ourselves so we don't block the blocking activity or MainActivity
                    if (foregroundPkg != packageName) {
                        withContext(Dispatchers.Main) {
                            triggerBlockScreen(foregroundPkg)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("FocusSessionService", "Error querying usage stats", e)
            }
        }
    }

    private fun triggerBlockScreen(packageName: String) {
        val blockIntent = Intent(applicationContext, Class.forName("com.example.ui.BlockScreenActivity")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra("blocked_package", packageName)
        }
        startActivity(blockIntent)
    }

    private fun playAlertAndVibrate() {
        try {
            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (vibrator != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(800, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(800)
                }
            }
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM) 
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val ringtone = RingtoneManager.getRingtone(applicationContext, alarmUri)
            ringtone?.play()
        } catch (e: Exception) {
            Log.e("FocusSessionService", "Failed to vibrate or play alert sound", e)
        }
    }

    private fun publishState() {
        _sessionState.value = FocusSessionState(
            studyDurationMinutes = studyDurationMin,
            breakDurationMinutes = breakDurationMin,
            totalBreaksConfigured = totalBreaksCount,
            breaksUsed = breaksUsedCount,
            totalFocusSecondsSpent = totalFocusSeconds,
            totalBreakSecondsSpent = totalBreakSeconds,
            remainingSeconds = secondsRemaining,
            currentPhase = phase,
            currentState = state,
            blockedApps = blockedAppsList,
            selectedSound = soundName,
            soundVolume = soundVolume,
            isSoundPlaying = soundPlaying,
            pauseMusicDuringBreaks = pauseMusicOnBreaks,
            startTime = sessionStartTime,
            presetName = presetName
        )
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Active Focus Session",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows real-time timer for ongoing study and break sessions."
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }

    private fun createNotification(): Notification {
        val title = if (phase == FocusPhase.STUDY) "Study Session Active" else "Break Time Active"
        val minutes = secondsRemaining / 60
        val seconds = secondsRemaining % 60
        val timeStr = String.format("%02d:%02d", minutes, seconds)
        val text = if (phase == FocusPhase.STUDY) "Keep studying! Focus timer: $timeStr" else "Relax and recharge. Break timer: $timeStr"

        val notificationIntent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Actions
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_media_play) // Fallback icon
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)

        // Quick control actions in notification
        if (phase == FocusPhase.STUDY) {
            if (state == FocusState.RUNNING) {
                val pauseIntent = Intent(this, FocusSessionService::class.java).apply { action = ACTION_PAUSE }
                val pPause = PendingIntent.getService(this, 1, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                builder.addAction(android.R.drawable.ic_media_pause, "Pause", pPause)
            } else if (state == FocusState.PAUSED) {
                val resumeIntent = Intent(this, FocusSessionService::class.java).apply { action = ACTION_RESUME }
                val pResume = PendingIntent.getService(this, 2, resumeIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                builder.addAction(android.R.drawable.ic_media_play, "Resume", pResume)
            }

            if (breaksUsedCount < totalBreaksCount) {
                val breakIntent = Intent(this, FocusSessionService::class.java).apply { action = ACTION_START_BREAK }
                val pBreak = PendingIntent.getService(this, 3, breakIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                builder.addAction(android.R.drawable.ic_media_next, "Take Break", pBreak)
            }
        } else if (phase == FocusPhase.BREAK) {
            val resumeIntent = Intent(this, FocusSessionService::class.java).apply { action = ACTION_END_BREAK_EARLY }
            val pResume = PendingIntent.getService(this, 4, resumeIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            builder.addAction(android.R.drawable.ic_media_play, "End Break Early", pResume)
        }

        val stopIntent = Intent(this, FocusSessionService::class.java).apply { action = ACTION_STOP }
        val pStop = PendingIntent.getService(this, 5, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        builder.addAction(android.R.drawable.ic_menu_close_clear_cancel, "Cancel Session", pStop)

        return builder.build()
    }

    private fun updateNotification() {
        val manager = getSystemService(NotificationManager::class.java)
        manager?.notify(NOTIFICATION_ID, createNotification())
    }

    override fun onDestroy() {
        soundSynthesizer.stop()
        timerJob?.cancel()
        databaseJob?.cancel()
        _sessionState.value = null
        super.onDestroy()
    }
}
