package com.sdnpk.fivepointone

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.sdnpk.fivepointone.ui.theme.FivePointOneTheme
import android.os.Handler
import android.os.Looper
import androidx.compose.ui.platform.LocalContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.MulticastSocket


@Composable
fun SpeakerDeviceScreen(navController: NavHostController) {
    // State variable to hold the main device's IP
    var mainIp by remember { mutableStateOf<String>("") }

    // Call the function to listen for the main device discovery
    ListenForMainDevice { ip ->
        mainIp = ip // Update the main device IP
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

        // Display the main device IP if found
        if (mainIp.isNotEmpty()) {
            Text("Main Device IP: $mainIp", style = MaterialTheme.typography.bodyLarge)
        } else {
            Text("Waiting for Main Device...")
        }
    }
}

@Composable
fun ListenForMainDevice(onMainDeviceFound: (String) -> Unit) {
    val context = LocalContext.current // This is used to get the context for Toast

    // Start the listening in a background thread
    LaunchedEffect(true) {
        Thread {
            try {
                val socket = MulticastSocket(9876)
                val group = InetAddress.getByName("224.0.0.1") // Multicast group address
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

                        // Call the onMainDeviceFound callback to update the UI with the IP
                        Handler(Looper.getMainLooper()).post {
                            onMainDeviceFound(mainIp) // Update the UI with the IP
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()  // Handle any errors
            }
        }.start() // Start the background thread to listen for the main device
    }
}
