package com.sdnpk.fivepointone.speaker_device

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel

class DiscoveryViewModel : ViewModel() {
    private val _mainDeviceIp = mutableStateOf<String?>(null)
    val mainDeviceIp: State<String?> = _mainDeviceIp

    fun setMainDeviceIp(ip: String) {
        _mainDeviceIp.value = ip
    }
}
