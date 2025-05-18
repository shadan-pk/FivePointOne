package com.sdnpk.fivepointone.main_device.connection

import com.sdnpk.fivepointone.data.SpeakerDevice
import org.json.JSONObject
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.UUID


fun sendPingRequest(speaker: SpeakerDevice, onLatencyMeasured: (Long?) -> Unit) {
    Thread {
        try {
            val socket = DatagramSocket()
            socket.soTimeout = 3000  // 3-second timeout

            val address = InetAddress.getByName(speaker.ip)

            val pingJson = JSONObject().apply {
                put("type", "ping_request")
                put("ping_id", UUID.randomUUID().toString()) // unique ID for future use
            }.toString()

            val data = pingJson.toByteArray()
            val packet = DatagramPacket(data, data.size, address, 6006)

            val startTime = System.nanoTime()
            socket.send(packet)

            val buffer = ByteArray(1024)
            val responsePacket = DatagramPacket(buffer, buffer.size)
            socket.receive(responsePacket)
            val endTime = System.nanoTime()

            val response = String(responsePacket.data, 0, responsePacket.length)
            val jsonResponse = JSONObject(response)

            if (jsonResponse.optString("type") == "ping_response") {
                val latencyMs = (endTime - startTime) / 1_000_000 // Convert ns to ms
                onLatencyMeasured(latencyMs)
            } else {
                onLatencyMeasured(null)
            }
            socket.close()
        } catch (e: Exception) {
            onLatencyMeasured(null)
        }
    }.start()
}
