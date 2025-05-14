package com.sdnpk.fivepointone.main_device

import android.util.Log
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.DatagramPacket
import java.net.InetAddress
import java.net.MulticastSocket

object DiscoveryListenerService {
    private const val MULTICAST_IP = "224.0.0.1"
    private const val MULTICAST_PORT = 45678

    private var listenerJob: Job? = null
    var discoveredSpeakers: MutableList<JSONObject> = mutableListOf()

    fun startListening() {
        listenerJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                val socket = MulticastSocket(MULTICAST_PORT)
                socket.joinGroup(InetAddress.getByName(MULTICAST_IP))

                val buffer = ByteArray(1024)
                while (isActive) {
                    val packet = DatagramPacket(buffer, buffer.size)
                    socket.receive(packet)
                    val received = String(packet.data, 0, packet.length)
                    val json = JSONObject(received)
                    Log.d("Discovery", "Speaker Found: $json")

                    synchronized(discoveredSpeakers) {
                        discoveredSpeakers.removeAll { it.getString("id") == json.getString("id") }
                        discoveredSpeakers.add(json)
                    }
                }
            } catch (e: Exception) {
                Log.e("DiscoveryListener", "Error: ${e.message}")
            }
        }
    }

    fun stopListening() {
        listenerJob?.cancel()
    }

}
