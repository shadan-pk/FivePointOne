package com.sdnpk.fivepointone.repository

//import com.sdnpk.fivepointone.model.SpeakerDevice
import com.sdnpk.fivepointone.config.Role
import com.sdnpk.fivepointone.data.SpeakerDevice
import kotlinx.coroutines.flow.Flow

interface SpeakerRepository {
    fun getSpeakersByPreset(presetId: String): Flow<List<SpeakerDevice>>
    suspend fun updateSpeakerRole(speakerId: String, role: Role)
    suspend fun updateLatency(speakerId: String, latencyMs: Int)
    suspend fun upsertSpeaker(speaker: SpeakerDevice)
    suspend fun deleteSpeaker(speakerId: String)
}
