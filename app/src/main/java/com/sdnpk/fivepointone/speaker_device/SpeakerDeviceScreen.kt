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
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            // 1) Acquire the multicast lock
            val wifi = context.applicationContext
                .getSystemService(Context.WIFI_SERVICE) as WifiManager
            val lock = wifi.createMulticastLock("discoveryLock").apply {
                setReferenceCounted(true)
                acquire()
            }

            var socket: MulticastSocket? = null
            try {
                // 2) Open and configure the socket once
                socket = MulticastSocket(9876).apply {
                    soTimeout = 5000                       // wait up to 5s
                    val group = InetAddress.getByName("224.0.0.1")
                    joinGroup(group)                       // join once
                }

                val buffer = ByteArray(256)
                val packet = DatagramPacket(buffer, buffer.size)

                // 3) Keep listening as long as the Composable is alive
                while (isActive) {
                    Log.d("Listening", "Waiting for multicast...")
                    try {
                        socket.receive(packet)
                        val message = String(buffer, 0, packet.length)
                        println("Received Message: $message")
                        Log.d("Listening", "Received: $message from ${packet.address.hostAddress}")

                        if (message == "FIVEPOINTONE_DISCOVERY") {
                            val mainIp = packet.address.hostAddress
                            println("Main Device Found: $mainIp")
                            onMainDeviceFound(mainIp)

                            // send response
                            DatagramSocket().use { responseSocket ->
                                val payload = "SPEAKER_RESPONSE:${getLocalIpAddress()}".toByteArray()
                                val resp = DatagramPacket(
                                    payload, payload.size,
                                    packet.address, 9877
                                )
                                responseSocket.send(resp)
                                println("Sent response to $mainIp:9877")
                            }
                        }
                    } catch (e: SocketTimeoutException) {
                        // No packet in 5s → loop again
                        println("No broadcast received. Retrying…")
                    }
                }
            } catch (e: Exception) {
                Log.e("MainDeviceListener", "Fatal listener error", e)

            } finally {
                // 4) Clean up
                try {
                    socket?.leaveGroup(InetAddress.getByName("224.0.0.1"))
                } catch (_: Exception) { /* ignore */ }
                socket?.close()
                lock.release()
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
