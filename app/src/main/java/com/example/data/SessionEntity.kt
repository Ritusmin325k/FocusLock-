package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val startTime: Long,
    val studyDurationMinutes: Int,
    val breakDurationMinutes: Int,
    val breaksConfigured: Int,
    val breaksUsed: Int,
    val actualFocusSeconds: Int,
    val actualBreakSeconds: Int,
    val presetName: String?,
    val appsBlockedCount: Int,
    val soundUsed: String?,
    val completed: Boolean
)

@Entity(tableName = "presets")
data class PresetEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val studyDurationMinutes: Int,
    val breakDurationMinutes: Int,
    val breaksCount: Int,
    val appsToBlockJson: String // Serialized list of apps to block
)
