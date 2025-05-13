package com.sdnpk.fivepointone.main_device

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

class BroadcastService : Service() {

    private var isBroadcasting = false
    private var broadcastThread: Thread? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startBroadcasting()
        return START_STICKY
    }

    override fun onDestroy() {
        stopBroadcasting()
        super.onDestroy()
    }

    internal fun startBroadcasting() {
        if (isBroadcasting) return
        isBroadcasting = true

        broadcastThread = Thread {
            try {
                val socket = DatagramSocket()
                socket.broadcast = true
                val group = InetAddress.getByName("224.0.0.1")
                val message = "FIVEPOINTONE_DISCOVERY".toByteArray()

                while (isBroadcasting) {
                    val packet = DatagramPacket(message, message.size, group, 9876)
                    socket.send(packet)
                    Log.d("BroadcastService", "Broadcasted message to $group:9876")
                    Thread.sleep(2000)
                }

                socket.close()
            } catch (e: Exception) {
                Log.e("BroadcastService", "Error broadcasting", e)
            }
        }

        broadcastThread?.start()
    }

    internal fun stopBroadcasting() {
        isBroadcasting = false
        broadcastThread?.interrupt()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}