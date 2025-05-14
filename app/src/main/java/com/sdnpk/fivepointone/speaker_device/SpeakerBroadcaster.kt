package com.sdnpk.fivepointone.speaker_device

import android.content.Context
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

object SpeakerBroadcaster {
    private const val MULTICAST_IP = "224.0.0.1"
    private const val MULTICAST_PORT = 45678
    private const val BROADCAST_INTERVAL = 1000L

    private var broadcastJob: Job? = null

    fun startBroadcasting(context: Context, deviceId: String, isBluetoothConnected: Boolean, isUsingPhoneSpeaker: Boolean) {
        broadcastJob = CoroutineScope(Dispatchers.IO).launch {
            val socket = DatagramSocket()
            val address = InetAddress.getByName(MULTICAST_IP)

            while (isActive) {
                val json = JSONObject().apply {
                    put("id", deviceId)
                    put("bluetooth_connected", isBluetoothConnected)
                    put("is_phone_speaker", isUsingPhoneSpeaker)
                    put("timestamp", System.currentTimeMillis())
                }

                val data = json.toString().toByteArray()
                val packet = DatagramPacket(data, data.size, address, MULTICAST_PORT)
                socket.send(packet)

                delay(BROADCAST_INTERVAL)
            }

            socket.close()
        }
    }

    fun stopBroadcasting() {
        broadcastJob?.cancel()
    }
}

