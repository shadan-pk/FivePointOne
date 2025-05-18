// MulticastUtils.kt
package com.sdnpk.fivepointone.utils

import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.MulticastSocket

fun startMulticastReceiver(context: Context, onMessageReceived: (String, String) -> Unit) {
    Thread {
        try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val lock = wifiManager.createMulticastLock("multicastLock")
            lock.setReferenceCounted(true)
            lock.acquire()

            val socket = MulticastSocket(5000)
            socket.joinGroup(InetAddress.getByName("239.1.2.3"))

            val buffer = ByteArray(1024)
            val packet = DatagramPacket(buffer, buffer.size)

            while (true) {
                socket.receive(packet)
                val msg = String(packet.data, 0, packet.length)
                val ip = packet.address.hostAddress
                onMessageReceived(ip, msg)
            }
        } catch (e: Exception) {
            Log.e("MulticastReceiver", "Error: ${e.message}")
        }
    }.start()
}


private var multicastSenderThread: Thread? = null
@Volatile private var isBroadcasting = false

fun startMulticastSender() {
    if (isBroadcasting) return  // Prevent multiple threads
    isBroadcasting = true

    multicastSenderThread = Thread {
        try {
            val group = InetAddress.getByName("239.1.2.3")
            val port = 5000
            val socket = MulticastSocket()

            while (isBroadcasting) {
                val message = "Hello from Main! Time: ${System.currentTimeMillis()}"
                val data = message.toByteArray()
                val packet = DatagramPacket(data, data.size, group, port)
                socket.send(packet)

                Log.d("MulticastSender", "Sent: $message")
                Thread.sleep(2000)
            }

            socket.close()
            Log.d("MulticastSender", "Broadcasting stopped")

        } catch (e: Exception) {
            Log.e("MulticastSender", "Error: ${e.message}")
        }
    }
    multicastSenderThread?.start()
}


fun stopMulticastSender() {
    isBroadcasting = false
    multicastSenderThread?.interrupt()
    multicastSenderThread = null
}

