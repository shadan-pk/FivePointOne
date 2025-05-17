package com.sdnpk.fivepointone.speaker_device.disconnnection

import android.os.Build
import android.util.Log
import org.json.JSONObject
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

fun sendDisconnectMessageToMainDevice(mainDeviceIp: String, mainDevicePort: Int = 6001) {
    try {
        val socket = DatagramSocket()
        val message = JSONObject().apply {
            put("type", "disconnect")
            put("id", Build.MODEL) // Or your deviceId
            put("timestamp", System.currentTimeMillis())
        }.toString()

        val buffer = message.toByteArray()
        val address = InetAddress.getByName(mainDeviceIp)
        val packet = DatagramPacket(buffer, buffer.size, address, mainDevicePort)

        socket.send(packet)
        socket.close()

        Log.d("Speaker", "Sent disconnect message to $mainDeviceIp:$mainDevicePort")
    } catch (e: Exception) {
        Log.e("Speaker", "Failed to send disconnect message: ${e.message}", e)
    }
}
