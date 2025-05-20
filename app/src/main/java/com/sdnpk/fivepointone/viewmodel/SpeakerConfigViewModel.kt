    package com.sdnpk.fivepointone.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sdnpk.fivepointone.config.Role
import com.sdnpk.fivepointone.data.SpeakerDevice
import com.sdnpk.fivepointone.repository.SpeakerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class SpeakerViewModel @Inject constructor(
    private val repository: SpeakerRepository
) : ViewModel() {

    private val _discoveredSpeakers = MutableStateFlow<List<SpeakerDevice>>(emptyList())
    val speakers: StateFlow<List<SpeakerDevice>> = _discoveredSpeakers

    fun loadSpeakersFromPreset(presetId: String) {
        viewModelScope.launch {
            repository.getSpeakersByPreset(presetId).collect {
                _discoveredSpeakers.value = it
            }
        }
    }

    fun updateAssignedRole(speakerId: String, role: Role) {
        _discoveredSpeakers.value = _discoveredSpeakers.value.map {
            if (it.id == speakerId) it.copy(assignedRole = role) else it
        }
        viewModelScope.launch {
            repository.updateSpeakerRole(speakerId, role)
        }
    }

    fun updateLatency(speakerId: String, latencyMs: Int) {
        _discoveredSpeakers.value = _discoveredSpeakers.value.map {
            if (it.id == speakerId) it.copy(latencyMs = latencyMs) else it
        }
        viewModelScope.launch {
            repository.updateLatency(speakerId, latencyMs)
        }
    }

    fun saveSpeaker(speaker: SpeakerDevice) {
        viewModelScope.launch {
            repository.upsertSpeaker(speaker)
        }
    }

    fun removeSpeaker(speakerId: String) {
        _discoveredSpeakers.value = _discoveredSpeakers.value.filterNot { it.id == speakerId }
        viewModelScope.launch {
            repository.deleteSpeaker(speakerId)
        }
    }

    fun reconnectSpeaker(speakerId: String) {
        // Implement logic for reconnecting if needed
    }

    fun markSpeakerAsConnected(speakerId: String) {
        _discoveredSpeakers.value = _discoveredSpeakers.value.map {
            if (it.id == speakerId) it.copy(isConnected = true) else it
        }

        val speaker = _discoveredSpeakers.value.find { it.id == speakerId }
        if (speaker != null) {
            viewModelScope.launch {
                try {
                    Log.d("SpeakerViewModel", "Saving connected speaker to DB: $speaker")
                    repository.upsertSpeaker(speaker)
                    Log.d("SpeakerViewModel", "Speaker saved successfully in DB")
                } catch (e: Exception) {
                    Log.e("SpeakerViewModel", "Error saving speaker to DB", e)
                }
            }
        } else {
            Log.w("SpeakerViewModel", "Speaker with id $speakerId not found in list")
        }
    }


}
