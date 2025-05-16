package com.sdnpk.fivepointone.main_device.connection

import android.util.Log
import com.sdnpk.fivepointone.data.SpeakerDevice
import org.json.JSONObject
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress


fun sendConnectRequest(speaker: SpeakerDevice, onAckReceived: (Boolean) -> Unit) {
    Thread {
        try {
            val socket = DatagramSocket()
            socket.soTimeout = 3000  // timeout 3 seconds waiting for ACK

            val address = InetAddress.getByName(speaker.ip)
            val connectJson = JSONObject().apply {
                put("type", "connect_request")
                put("device_id", "MainDeviceID") // Replace with your main device ID
            }.toString()

            val data = connectJson.toByteArray()
            val packet = DatagramPacket(data, data.size, address, 6000)  // speaker's UDP port
            socket.send(packet)
            Log.d("UnicastConnect", "Sent connect_request to ${speaker.ip}")

            // Wait for ACK response
            val buffer = ByteArray(1024)
            val responsePacket = DatagramPacket(buffer, buffer.size)
            socket.receive(responsePacket)

            val response = String(responsePacket.data, 0, responsePacket.length)
            Log.d("UnicastConnect", "Received response: $response")

            val jsonResponse = JSONObject(response)
            val type = jsonResponse.optString("type")
            val ackDeviceId = jsonResponse.optString("device_id")

            if (type == "connect_ack" && ackDeviceId.isNotEmpty()) {
                Log.d("UnicastConnect", "ACK received from $ackDeviceId")
                onAckReceived(true)
            } else {
                onAckReceived(false)
            }
            socket.close()
        } catch (e: Exception) {
            Log.e("UnicastConnect", "Error sending connect request: ${e.message}")
            onAckReceived(false)
        }
    }.start()
}
