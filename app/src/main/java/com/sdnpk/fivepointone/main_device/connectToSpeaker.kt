package com.sdnpk.fivepointone.main_device

// For Coroutines
import android.util.Log
import com.sdnpk.fivepointone.data.SpeakerDevice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// For Networking
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

// For JSON (org.json library)
import org.json.JSONObject


fun connectToSpeaker(speaker: SpeakerDevice) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val socket = DatagramSocket()
            val connectMsg = "connect_request".toByteArray()
            val address = InetAddress.getByName(speaker.ip)
            Log.d("SpeakerConnection", "Connecting to ${speaker.id} at ${speaker.ip}")
            // Send connect request
            val packet = DatagramPacket(connectMsg, connectMsg.size, address, 5005)
            socket.send(packet)

            // Wait for ACK
            val buffer = ByteArray(1024)
            val response = DatagramPacket(buffer, buffer.size)
            socket.soTimeout = 2000 // wait 2s max
            socket.receive(response)

            val responseText = String(response.data, 0, response.length)
            if (responseText.contains("connect_ack")) {
                println("ACK received from ${speaker.id}")
                Log.d("SpeakerConnection", "ACK received from ${speaker.id}")
                // Measure latency with 3 pings
                val latencies = mutableListOf<Long>()
                repeat(3) {
                    val pingTime = System.currentTimeMillis()
                    val ping = "ping".toByteArray()
                    val pingPacket = DatagramPacket(ping, ping.size, address, 5005)
                    socket.send(pingPacket)

                    val pongBuffer = ByteArray(1024)
                    val pongPacket = DatagramPacket(pongBuffer, pongBuffer.size)
                    socket.receive(pongPacket)
                    val latency = System.currentTimeMillis() - pingTime
                    latencies.add(latency)
                }

                val avgLatency = latencies.average().toInt()

                withContext(Dispatchers.Main) {
                    // Update speaker state in list (you should use a MutableState list)
                    speaker.latencyMs = avgLatency
                    speaker.connected = true
                }
            }
        } catch (e: Exception) {
            println("Connection to ${speaker.id} failed: ${e.message}")
        }
    }
}
