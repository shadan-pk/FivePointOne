package com.sdnpk.fivepointone.speaker_device.connection

import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

class SpeakerListener(
    private val port: Int,
    private val deviceId: String,
    private val onConnectionRequest: (mainDeviceIp: String) -> Unit,
    private val onAcceptConnectionSignal: () -> Unit
) {
    private var socket: DatagramSocket? = null
    private var mainDeviceAddress: InetAddress? = null
    private var mainDevicePort: Int = -1
    private var connected = false

    fun startListening(coroutineScope: CoroutineScope) {
        socket = DatagramSocket(port)
        CoroutineScope(Dispatchers.IO).launch {
            val buffer = ByteArray(1024)
            while (true) {
                try {
                    val packet = DatagramPacket(buffer, buffer.size)
                    socket?.receive(packet)
                    val msgStr = String(packet.data, 0, packet.length)
                    val json = JSONObject(msgStr)
                    val type = json.optString("type")

                    if (type == "connect_request") {
                        mainDeviceAddress = packet.address
                        mainDevicePort = packet.port

                        // Notify UI to show "Accept Connection" button
                        onConnectionRequest(mainDeviceAddress!!.hostAddress)

                        // Wait here until UI signals acceptance
                        while (!connected) {
                            delay(100)
                        }

                        // Send ACK after acceptance
                        sendConnectAck()

                    } else if (type == "ping" && connected) {
                        // Respond to ping with pong immediately
                        sendPong(packet.address, packet.port)
                    }

                    // Handle other message types (role assignment) as needed

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun acceptConnection() {
        connected = true
    }

    private fun sendConnectAck() {
        val ackJson = JSONObject().apply {
            put("type", "connect_ack")
            put("device_id", deviceId)
        }.toString()
        val data = ackJson.toByteArray()
        val packet = DatagramPacket(data, data.size, mainDeviceAddress, mainDevicePort)
        socket?.send(packet)
    }

    private fun sendPong(addr: InetAddress, port: Int) {
        val pongJson = JSONObject().apply {
            put("type", "pong")
            put("device_id", deviceId)
        }.toString()
        val data = pongJson.toByteArray()
        val packet = DatagramPacket(data, data.size, addr, port)
        socket?.send(packet)
    }

    fun stopListening() {
        socket?.close()
    }
}
