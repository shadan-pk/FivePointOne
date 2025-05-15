package com.sdnpk.fivepointone.main_device

import androidx.lifecycle.ViewModel
import com.sdnpk.fivepointone.data.SpeakerDevice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONObject

class MainDeviceViewModel : ViewModel() {
    private val _discoveredSpeakers = MutableStateFlow<List<SpeakerDevice>>(emptyList())
    val discoveredSpeakers: StateFlow<List<SpeakerDevice>> = _discoveredSpeakers

    fun addSpeaker(newSpeaker: SpeakerDevice) {
        val updatedList = _discoveredSpeakers.value.toMutableList()
        val index = updatedList.indexOfFirst { it.id == newSpeaker.id }
        if (index != -1) {
            updatedList[index] = newSpeaker
        } else {
            updatedList.add(newSpeaker)
        }
        _discoveredSpeakers.value = updatedList
    }
}
