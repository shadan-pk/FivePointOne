package com.sdnpk.fivepointone

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.MulticastSocket

@Composable
fun SpeakerDeviceScreen(navController: NavHostController) {
    var mainIp by remember { mutableStateOf("") }

    LaunchedEffect(true) {
        listenForMainDevice { ip ->
            mainIp = ip
            sendResponseToMainDevice(ip)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Speaker Device Screen")

        Spacer(modifier = Modifier.height(24.dp))

        if (mainIp.isNotEmpty()) {
            Text("Main Device IP: $mainIp", style = MaterialTheme.typography.bodyLarge)
        } else {
            Text("Waiting for Main Device...")
        }
    }
}

suspend fun listenForMainDevice(onMainDeviceFound: (String) -> Unit) {
    withContext(Dispatchers.IO) {
        try {
            val socket = MulticastSocket(9876)
            val group = InetAddress.getByName("224.0.0.1")
            socket.joinGroup(group)

            val buffer = ByteArray(256)

            while (true) {
                val packet = DatagramPacket(buffer, buffer.size)
                socket.receive(packet)
                val message = String(packet.data, 0, packet.length)
                println("Received Message: $message")

                if (message == "FIVEPOINTONE_DISCOVERY") {
                    val mainIp = packet.address.hostAddress
                    println("Main Device Found: $mainIp")

                    withContext(Dispatchers.Main) {
                        onMainDeviceFound(mainIp)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

fun sendResponseToMainDevice(mainIp: String) {
    Thread {
        try {
            val responseMessage = "SPEAKER_DEVICE_RESPONSE|RearLeft" // Customize per speaker role
            val responseData = responseMessage.toByteArray()
            val packet = DatagramPacket(
                responseData,
                responseData.size,
                InetAddress.getByName(mainIp),
                9877 // Main device should listen on this port
            )
            val socket = DatagramSocket()
            socket.send(packet)
            println("Sent response to Main Device at $mainIp")
            socket.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }.start()
}
