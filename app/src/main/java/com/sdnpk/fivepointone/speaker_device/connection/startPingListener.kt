package com.sdnpk.fivepointone.speaker_device.connection

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import org.json.JSONObject
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.SocketException

class UdpPingService : Service() {

    @Volatile
    private var running = true
    private var socket: DatagramSocket? = null
    private lateinit var listenThread: Thread

    override fun onCreate() {
        super.onCreate()
        startUdpListener()
    }

    private fun startUdpListener() {
        listenThread = Thread {
            try {
                socket = DatagramSocket(6006)
                while (running) {
                    try {
                        val buffer = ByteArray(1024)
                        val packet = DatagramPacket(buffer, buffer.size)
                        socket?.receive(packet)

                        val receivedData = String(packet.data, 0, packet.length)
                        val json = JSONObject(receivedData)

                        if (json.optString("type") == "ping_request") {
                            Log.d("UdpPingService", "Ping request from ${packet.address.hostAddress}")

                            val responseJson = JSONObject().apply {
                                put("type", "ping_response")
                            }.toString()

                            val responseData = responseJson.toByteArray()
                            val responsePacket = DatagramPacket(responseData, responseData.size, packet.address, packet.port)
                            socket?.send(responsePacket)

                            Log.d("UdpPingService", "Ping response sent to ${packet.address.hostAddress}")
                        }
                    } catch (se: SocketException) {
                        if (running) {
                            Log.e("UdpPingService", "Socket exception: ${se.message}")
                        }
                        break // Exit loop if socket closed or error occurred
                    } catch (e: Exception) {
                        Log.e("UdpPingService", "Error processing packet: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                Log.e("UdpPingService", "UDP listener error: ${e.message}")
            } finally {
                socket?.close()
            }
        }
        listenThread.start()
    }

    override fun onDestroy() {
        running = false
        socket?.close()    // This will unblock receive()
        listenThread.interrupt()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }
}
