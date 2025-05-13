package com.sdnpk.fivepointone.main_device

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import java.net.DatagramPacket
import java.net.DatagramSocket

fun listenForSpeakerResponses(onSpeakerFound: (String) -> Unit) {
    Thread {
        try {
            val socket = DatagramSocket(9877)
            val buffer = ByteArray(256)
            while (true) {
                val packet = DatagramPacket(buffer, buffer.size)
                socket.receive(packet)
                val message = String(packet.data, 0, packet.length)
                Log.d("SpeakerListener", "Received Response: $message")
                if (message.startsWith("SPEAKER_RESPONSE:")) {
                    val speakerIp = message.removePrefix("SPEAKER_RESPONSE:")
                    Log.d("SpeakerListener", "Speaker Found: $speakerIp")
                    Handler(Looper.getMainLooper()).post {
                        onSpeakerFound(speakerIp)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("SpeakerListener", "Error listening for speakers", e)
        }
    }.start()
}

@Composable
fun ConnectedSpeakersList(connectedSpeakers: List<String>) {
    Column {
        Text("Connected Speakers:", style = MaterialTheme.typography.titleMedium)
        connectedSpeakers.forEach { speaker ->
            Text(speaker, style = MaterialTheme.typography.bodyMedium)
        }
    }
}