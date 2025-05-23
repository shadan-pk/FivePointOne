package com.sdnpk.fivepointone.main_device

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sdnpk.fivepointone.data.SpeakerDevice
import com.sdnpk.fivepointone.utils.stopMulticastSender
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONObject

class MainDeviceViewModel : ViewModel() {
    private val _discoveredSpeakers = MutableStateFlow<List<SpeakerDevice>>(emptyList())
    val discoveredSpeakers: StateFlow<List<SpeakerDevice>> = _discoveredSpeakers
    private val lastSeenMap = mutableMapOf<String, Long>()
    private val heartbeatTimeoutMs = 3000L
    private var checkJob: Job? = null

    init {
        startPeriodicCheck()
    }

    private fun startPeriodicCheck() {
        checkJob = viewModelScope.launch {
            while (isActive) {
                delay(4000)
                checkInactiveSpeakers(heartbeatTimeoutMs)
            }
        }
    }

    fun addSpeaker(newSpeaker: SpeakerDevice) {
        val updatedList = _discoveredSpeakers.value.toMutableList()
        val index = updatedList.indexOfFirst { it.id == newSpeaker.id }
        if (index != -1) {
            val existing = updatedList[index]
            updatedList[index] = newSpeaker.copy(isConnected = existing.isConnected)
        } else {
            updatedList.add(newSpeaker)
        }
        _discoveredSpeakers.value = updatedList
    }

//    fun markSpeakerAsConnected(speakerId: String) {
//        _discoveredSpeakers.value = _discoveredSpeakers.value.map {
//            if (it.id == speakerId) it.copy(isConnected = true) else it
//        }
//    }


    fun disconnectAllSpeakers() {
        _discoveredSpeakers.value = _discoveredSpeakers.value.map {
            if (it.isConnected) it.copy(isConnected = false) else it
        }
        stopMulticastSender()
    }

    fun updateSpeakerHeartbeat(speaker: SpeakerDevice) {
        lastSeenMap[speaker.id] = System.currentTimeMillis()
        addSpeaker(speaker)
    }

    fun checkInactiveSpeakers(timeoutMs: Long = heartbeatTimeoutMs) {
        val now = System.currentTimeMillis()
        val updatedList = _discoveredSpeakers.value.map { speaker ->
            val lastSeen = lastSeenMap[speaker.id] ?: 0L
            val isStillAlive = now - lastSeen <= timeoutMs
            if (!isStillAlive && speaker.isConnected) {
                speaker.copy(isConnected = false)
            } else {
                speaker
            }
        }
        _discoveredSpeakers.value = updatedList
    }

    fun getSpeakerById(id: String): SpeakerDevice? {
        return _discoveredSpeakers.value.find { it.id == id }
    }

    override fun onCleared() {
        super.onCleared()
        checkJob?.cancel()
    }
}