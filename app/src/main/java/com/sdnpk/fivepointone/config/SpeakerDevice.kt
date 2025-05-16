package com.sdnpk.fivepointone.data

import com.sdnpk.fivepointone.config.Role

//data class SpeakerDevice(
//    val id: String,
//    val ip: String,
//    val latencyMs: Int,
//    val bluetoothConnected: Boolean,
//    var assignedRole: Role? = null
//)
data class SpeakerDevice(
    val id: String,
    val ip: String,
    val bluetoothConnected: Boolean,
    var latencyMs: Int? = null,
    var assignedRole: Role? = null,
    var connected: Boolean = false
)
