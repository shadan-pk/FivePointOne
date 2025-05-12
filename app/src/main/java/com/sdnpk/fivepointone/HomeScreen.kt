package com.sdnpk.fivepointone

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

@Composable
fun HomeScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("FivePointOne")

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = {
            startBroadcasting()  // ✅ Call it here
            navController.navigate("main_device")
        }) {
            Text("Act as Main Device")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            navController.navigate("speaker_device")
        }) {
            Text("Act as Speaker Device")
        }
    }
}

fun startBroadcasting() {
    Log.d("Broadcasting", "Starting Broadcasting Thread...")
    Thread {
        try {
            val socket = DatagramSocket()
            val group = InetAddress.getByName("224.0.0.1") // Multicast group IP
            val message = "FIVEPOINTONE_DISCOVERY".toByteArray()

            while (true) {
                val packet = DatagramPacket(message, message.size, group, 9876)
                socket.send(packet)
                Log.d("Broadcasting", "Broadcasted message to $group:9876")
                Thread.sleep(2000)
            }
        } catch (e: Exception) {
            Log.e("Broadcasting", "Error broadcasting", e)
        }
    }.start()
}
