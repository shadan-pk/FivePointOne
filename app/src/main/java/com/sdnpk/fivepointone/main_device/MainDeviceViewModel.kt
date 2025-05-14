package com.sdnpk.fivepointone.main_device

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONObject

class MainDeviceViewModel : ViewModel() {
    private val _discoveredSpeakers = MutableStateFlow<List<JSONObject>>(emptyList())
    val discoveredSpeakers: StateFlow<List<JSONObject>> = _discoveredSpeakers

    // Method to add a discovered speaker
    fun addSpeaker(speaker: JSONObject) {
        _discoveredSpeakers.value = _discoveredSpeakers.value + speaker
    }
}
