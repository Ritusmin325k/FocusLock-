package com.example.data

import kotlinx.coroutines.flow.Flow

class SessionRepository(private val sessionDao: SessionDao) {
    val allSessions: Flow<List<SessionEntity>> = sessionDao.getAllSessions()
    val allPresets: Flow<List<PresetEntity>> = sessionDao.getAllPresets()

    suspend fun insertSession(session: SessionEntity): Long {
        return sessionDao.insertSession(session)
    }

    suspend fun deleteSession(session: SessionEntity) {
        sessionDao.deleteSession(session)
    }

    suspend fun insertPreset(preset: PresetEntity) {
        sessionDao.insertPreset(preset)
    }

    suspend fun deletePresetById(id: Int) {
        sessionDao.deletePresetById(id)
    }
}
