package com.sdnpk.fivepointone.speaker_device

import android.content.Context
import android.net.wifi.WifiManager
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.Inet4Address
import java.net.InetAddress
import java.net.MulticastSocket
import java.net.NetworkInterface
import java.net.SocketTimeoutException

@Composable
fun SpeakerDeviceScreen() {
    // State variable to hold the main device's IP
    var mainIp by remember { mutableStateOf<String>("") }

    // Call the function to listen for the main device discovery
    ListenForMainDevice(context = LocalContext.current) {
        mainIp ->
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
fun ListenForMainDevice(
    context: Context,
    onMainDeviceFound: (String) -> Unit
) {
    val scope = rememberCoroutineScope()

    LaunchedEffect(true) {
        scope.launch(Dispatchers.IO) {
            var socket: MulticastSocket? = null
            var multicastLock: WifiManager.MulticastLock? = null
            try {
                // Acquire Multicast Lock
                val wifi = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                multicastLock = wifi.createMulticastLock("fivepointone_multicast_lock").apply {
                    setReferenceCounted(true)
                    acquire()
                }

                val group = InetAddress.getByName("224.0.0.1")
                socket = MulticastSocket(9876)
                socket.joinGroup(group)
                socket.soTimeout = 5000 // 5 seconds timeout

                val buffer = ByteArray(256)

                while (isActive) {
                    try {
                        Log.d("Listening", "Waiting for multicast...")
                        val packet = DatagramPacket(buffer, buffer.size)
                        socket.receive(packet)

                        val message = String(packet.data, 0, packet.length)
                        Log.d("Listening", "Received: $message from ${packet.address.hostAddress}")

                        if (message == "FIVEPOINTONE_DISCOVERY") {
                            val mainIp = packet.address.hostAddress
                            Log.d("Listening", "Main Device Found: $mainIp")
                            onMainDeviceFound(mainIp)

                            // Send response back
                            DatagramSocket().use { responseSocket ->
                                val responseMessage = "SPEAKER_RESPONSE:${getLocalIpAddress()}".toByteArray()
                                val responsePacket = DatagramPacket(
                                    responseMessage,
                                    responseMessage.size,
                                    packet.address,
                                    9877
                                )
                                responseSocket.send(responsePacket)
                                Log.d("Listening", "Sent response to $mainIp:9877")
                            }
                        }
                    } catch (e: SocketTimeoutException) {
                        println("No broadcast received. Retrying…")
                    }
                }
            } catch (e: Exception) {
                Log.e("Listening", "Error in listening", e)
            } finally {
                socket?.leaveGroup(InetAddress.getByName("224.0.0.1"))
                socket?.close()
                multicastLock?.release()
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
