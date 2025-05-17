package com.sdnpk.fivepointone.main_device.disconnection

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.DatagramPacket
import java.net.DatagramSocket

//fun startListeningForDisconnects(onSpeakerDisconnected: (String) -> Unit) {
//    CoroutineScope(Dispatchers.IO).launch {
//        val socket = DatagramSocket(6001)
//        val buffer = ByteArray(1024)
//
//        while (true) {
//            try {
//                val packet = DatagramPacket(buffer, buffer.size)
//                socket.receive(packet)
//                val message = String(packet.data, 0, packet.length)
//
//                val json = JSONObject(message)
//                if (json.getString("type") == "disconnect") {
//                    val speakerId = json.getString("id")
//                    Log.d("MainDevice", "Received disconnect from $speakerId")
//                    withContext(Dispatchers.Main) {
//                        onSpeakerDisconnected(speakerId)
//                    }
//                }
//
//            } catch (e: Exception) {
//                Log.e("MainDevice", "Error receiving disconnect", e)
//            }
//        }
//        // socket.close() — optional, but unreachable unless you implement a way to shut it down
//    }
//}
