package com.sdnpk.fivepointone.speaker_device

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.Inet4Address
import java.net.InetAddress
import java.net.MulticastSocket
import java.net.NetworkInterface

@Composable
fun SpeakerDeviceScreen() {
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
    val scope = rememberCoroutineScope() // Use CoroutineScope for async operations

    // Listen for the main device using LaunchedEffect to launch the background task
    LaunchedEffect(true) {
        // Using coroutine instead of Thread for better lifecycle management
        scope.launch {
            var socket: MulticastSocket? = null
            try {
                socket = MulticastSocket(9876)
                val group = InetAddress.getByName("224.0.0.1")
                socket.joinGroup(group)
                val buffer = ByteArray(256)

                while (!isActive) { // Keep listening until the composable is disposed
                    val packet = DatagramPacket(buffer, buffer.size)
                    socket.receive(packet)
                    val message = String(packet.data, 0, packet.length)
                    println("Received Message: $message")
                    if (message == "FIVEPOINTONE_DISCOVERY") {
                        val mainIp = packet.address.hostAddress
                        println("Main Device Found: $mainIp")
                        onMainDeviceFound(mainIp) // Update the UI state

                        // Send response back to the main device
                        var responseSocket: DatagramSocket? = null
                        try {
                            responseSocket = DatagramSocket()
                            val responseMessage =
                                "SPEAKER_RESPONSE:${getLocalIpAddress()}".toByteArray()
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
        }
    }
}

// Helper function to get the local IP address
fun getLocalIpAddress(): String {
    try {
        val interfaces = NetworkInterface.getNetworkInterfaces()
        while (interfaces.hasMoreElements()) {
            val networkInterface = interfaces.nextElement()
            val addresses = networkInterface.inetAddresses
            while (addresses.hasMoreElements()) {
                val address = addresses.nextElement()
                if (!address.isLoopbackAddress && address is Inet4Address) {
                    return address.hostAddress
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return "Unknown"
}
