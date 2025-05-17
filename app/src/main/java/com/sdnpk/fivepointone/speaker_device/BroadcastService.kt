package com.sdnpk.fivepointone.speaker_device

import android.util.Log
import org.json.JSONObject
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

// If your SpeakerBroadcaster is an object like this:
object SpeakerBroadcaster {
    private var isBroadcasting = false
    private var broadcastThread: Thread? = null

    fun startBroadcasting(deviceId: String, bluetoothConnected: Boolean, isPhoneSpeaker: Boolean) {
        if (isBroadcasting) return

        isBroadcasting = true
        broadcastThread = Thread {
            try {
                val socket = DatagramSocket()
                val group = InetAddress.getByName("239.1.2.3")
                val port = 6000

                while (isBroadcasting) {
                    val json = JSONObject().apply {
                        put("id", deviceId)
                        put("bluetooth_connected", bluetoothConnected)
                        put("is_phone_speaker", isPhoneSpeaker)
                        put("timestamp", System.currentTimeMillis())
                    }
                    val message = json.toString().toByteArray()
                    val packet = DatagramPacket(message, message.size, group, port)
                    socket.send(packet)

                    Log.d("SpeakerBroadcaster", "Broadcasted: $json")
                    Thread.sleep(1500)
                }
                socket.close()
            } catch (e: Exception) {
                Log.e("SpeakerBroadcaster", "Error in broadcasting", e)
            }
        }
        broadcastThread?.start()
    }

    fun stopBroadcasting() {
        isBroadcasting = false
        broadcastThread?.interrupt()
        Log.d("SpeakerBroadcaster", "Broadcasting stopped")
    }
}