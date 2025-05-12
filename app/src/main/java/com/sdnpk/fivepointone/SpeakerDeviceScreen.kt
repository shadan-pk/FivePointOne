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
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.Inet4Address
import java.net.InetAddress
import java.net.MulticastSocket
import java.net.NetworkInterface


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
    val context = LocalContext.current
    LaunchedEffect(true) {
        Thread {
            var socket: MulticastSocket? = null
            try {
                socket = MulticastSocket(9876)
                val group = InetAddress.getByName("224.0.0.1")
                socket.joinGroup(group)
                val buffer = ByteArray(256)
                while (!Thread.currentThread().isInterrupted) {
                    val packet = DatagramPacket(buffer, buffer.size)
                    socket.receive(packet)
                    val message = String(packet.data, 0, packet.length)
                    println("Received Message: $message")
                    if (message == "FIVEPOINTONE_DISCOVERY") {
                        val mainIp = packet.address.hostAddress
                        println("Main Device Found: $mainIp")
                        Handler(Looper.getMainLooper()).post {
                            if (mainIp != null) {
                                onMainDeviceFound(mainIp)
                            }
                        }

                        // Send response back to the main device
                        var responseSocket: DatagramSocket? = null
                        try {
                            responseSocket = DatagramSocket()
                            val responseMessage = "SPEAKER_RESPONSE:${getLocalIpAddress()}".toByteArray()
                            val responsePacket = DatagramPacket(
                                responseMessage,
                                responseMessage.size,
                                packet.address,
                                9877
                            )
                            responseSocket.send(responsePacket)
                            println("Sent response to $mainIp:9877")
                        } catch (e: Exception) {
                            Log.e("SpeakerResponse", "Error sending response", e)
                        } finally {
                            responseSocket?.close()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("MainDeviceListener", "Error listening for main device", e)
            } finally {
                socket?.leaveGroup(InetAddress.getByName("224.0.0.1"))
                socket?.close()
            }
        }.start()
    }
}

