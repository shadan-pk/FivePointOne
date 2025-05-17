package com.sdnpk.fivepointone.main_device

import android.content.Context
import android.util.Log
import com.sdnpk.fivepointone.data.SpeakerDevice
import org.json.JSONObject
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.MulticastSocket

fun startSpeakerDiscoveryReceiver(viewModel: MainDeviceViewModel) {
    Thread {
        try {
            val group = InetAddress.getByName("239.1.2.3")  // Must match broadcaster group
            val socket = MulticastSocket(6000)             // Must match broadcaster port
            socket.joinGroup(group)

            val buffer = ByteArray(1024)
            val packet = DatagramPacket(buffer, buffer.size)

            while (true) {
                socket.receive(packet)
                val message = String(packet.data, 0, packet.length)
                Log.d("DiscoveryReceiver", "Received: $message")

                val json = JSONObject(message)
                val id = json.optString("id")
                val btConnected = json.optBoolean("bluetooth_connected")
                val isPhoneSpeaker = json.optBoolean("is_phone_speaker")
                val timestamp = json.optLong("timestamp")

                val speaker = SpeakerDevice(
                    id = id,
                    ip = packet.address?.hostAddress ?: "0.0.0.0",
                    latencyMs = 0,  // You can add latency calculation later
                    bluetoothConnected = btConnected,
                    assignedRole = null
                )

//                viewModel.addSpeaker(speaker)
                viewModel.updateSpeakerHeartbeat(speaker)
            }
        } catch (e: Exception) {
            Log.e("DiscoveryReceiver", "Error in multicast receive", e)
        }
    }.start()
}


