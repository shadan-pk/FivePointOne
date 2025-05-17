package com.sdnpk.fivepointone.speaker_device.connection

import android.util.Log
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

class SpeakerUnicastListener(
    private val port: Int,
    private val deviceId: String,
    private val onConnectRequestReceived: (String) -> Unit // Pass sender IP to UI
) {
    private var socket: DatagramSocket? = null
    private var isRunning = false
    private var requesterAddress: InetAddress? = null
    private var requesterPort: Int = -1
    private var accepted = false
    private var listenerJob: Job? = null

    fun start(scope: CoroutineScope) {
        socket = DatagramSocket(port)
        isRunning = true

        listenerJob = scope.launch(Dispatchers.IO) {
            val buffer = ByteArray(1024)
            while (isRunning) {
                try {
                    val packet = DatagramPacket(buffer, buffer.size)
                    socket?.receive(packet)

                    val msg = String(packet.data, 0, packet.length)
                    Log.d("SpeakerListener", "Received: $msg from ${packet.address.hostAddress}")

                    val json = JSONObject(msg)
                    val type = json.optString("type")

                    if (type == "connect_request") {
                        requesterAddress = packet.address
                        requesterPort = packet.port
                        accepted = false

                        onConnectRequestReceived(requesterAddress!!.hostAddress)

                        // Wait for user acceptance from UI
                        while (!accepted && isActive) {
                            delay(100)
                        }

                        sendAck()
                    }

                } catch (e: Exception) {
                    Log.e("SpeakerListener", "Error: ${e.message}")
                }
            }
        }
    }

    fun acceptConnection() {
        Log.d("SpeakerListener", "Connection accepted by user")
        accepted = true
    }

    private fun sendAck() {
        try {
            val ackJson = JSONObject().apply {
                put("type", "connect_ack")
                put("device_id", deviceId)
            }.toString()

            val data = ackJson.toByteArray()
            val ackPacket = DatagramPacket(data, data.size, requesterAddress, requesterPort)
            socket?.send(ackPacket)
            Log.d("SpeakerListener", "Sent connect_ack to ${requesterAddress?.hostAddress}")
        } catch (e: Exception) {
            Log.e("SpeakerListener", "Error sending ACK: ${e.message}")
        }
    }

    fun stop() {
        isRunning = false
        listenerJob?.cancel()
        socket?.close()
    }
}
