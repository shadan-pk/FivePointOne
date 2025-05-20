package com.sdnpk.fivepointone.repository

import com.sdnpk.fivepointone.config.Role
import com.sdnpk.fivepointone.data.SpeakerDevice
import com.sdnpk.fivepointone.data.db.dao.SpeakerConfigDao
import com.sdnpk.fivepointone.data.db.entity.SpeakerConfigEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpeakerRepositoryImpl @Inject constructor(
    private val dao: SpeakerConfigDao
) : SpeakerRepository {

    override fun getSpeakersByPreset(presetId: String): Flow<List<SpeakerDevice>> {
        // You might want to filter by roomName or other preset logic here,
        // but since your DAO's getAll() returns all configs, let's fetch all and convert.
        return flow {
            val entities = dao.getAll()
            emit(entities.map { it.toSpeakerDevice() })
        }
    }

    override suspend fun updateSpeakerRole(speakerId: String, role: Role) {
        val existing = dao.getById(speakerId)
        if (existing != null) {
            val updated = existing.copy(assignedRole = role)
            dao.insertOrUpdate(updated)
        }
    }

    override suspend fun updateLatency(speakerId: String, latencyMs: Int) {
        // Latency isn't stored in your DB currently, so you could extend your entity if needed.
        // For now, this method can be left empty or throw UnsupportedOperationException.
    }

    override suspend fun upsertSpeaker(speaker: SpeakerDevice) {
        val entity = speaker.toSpeakerConfigEntity()
        dao.insertOrUpdate(entity)
    }

    override suspend fun deleteSpeaker(speakerId: String) {
        val existing = dao.getById(speakerId)
        if (existing != null) {
            dao.delete(existing)
        }
    }
}

// Helper extension functions for conversions
private fun SpeakerConfigEntity.toSpeakerDevice() = SpeakerDevice(
    id = id,
    ip = ip,
    latencyMs = 0,  // no latency info stored in DB, set default 0 or adjust
    bluetoothConnected = false,  // no bluetooth info stored, default false
    assignedRole = assignedRole,
    isConnected = false
)

private fun SpeakerDevice.toSpeakerConfigEntity() = SpeakerConfigEntity(
    id = id,
    ip = ip,
    assignedRole = assignedRole,
    roomName = ""  // You may want to handle roomName properly if you use it
)
