package com.sdnpk.fivepointone.main_device

import android.util.Log
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

class BroadcastService {
    private var isBroadcasting = false
    private var broadcastThread: Thread? = null

    // Start broadcasting
    fun startBroadcasting() {
        if (isBroadcasting) return // Don't start if already broadcasting

        Log.d("Broadcasting", "Starting Broadcasting Thread...")
        broadcastThread = Thread {
            try {
                val socket = DatagramSocket()
                socket.broadcast = true // Enable broadcast
                val group = InetAddress.getByName("224.0.0.1")
                val message = "FIVEPOINTONE_DISCOVERY".toByteArray()
                isBroadcasting = true

                while (isBroadcasting) {
                    val packet = DatagramPacket(message, message.size, group, 9876)
                    socket.send(packet)
                    Log.d("Broadcasting", "Broadcasted message to $group:9876")
                    Thread.sleep(2000)
                }
                socket.close()
            } catch (e: Exception) {
                Log.e("Broadcasting", "Error broadcasting", e)
            }
        }
        broadcastThread?.start()
    }

    // Stop broadcasting
    fun stopBroadcasting() {
        isBroadcasting = false
        broadcastThread?.interrupt()
        Log.d("Broadcasting", "Broadcasting Stopped.")
    }
}
