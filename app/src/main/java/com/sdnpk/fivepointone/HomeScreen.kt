package com.sdnpk.fivepointone

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.concurrent.atomic.AtomicBoolean

//@Composable
//fun HomeScreen(navController: NavController) {
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(24.dp),
//        verticalArrangement = Arrangement.Center,
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Text("FivePointOne")
//
//        Spacer(modifier = Modifier.height(24.dp))
//
//        Button(onClick = {
//            startBroadcasting()  // ✅ Call it here
//            navController.navigate("main_device")
//        }) {
//            Text("Act as Main Device")
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        Button(onClick = {
//            navController.navigate("speaker_device")
//        }) {
//            Text("Act as Speaker Device")
//        }
//    }
//}

@Composable
fun HomeScreen(navController: NavHostController) {
    androidx.compose.foundation.layout.Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        androidx.compose.material3.Text(
            "5.1 Audio System",
            style = androidx.compose.material3.MaterialTheme.typography.headlineLarge
        )

        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(32.dp))

        androidx.compose.material3.Button(
            onClick = {
                startBroadcasting()
                navController.navigate("main_device")
            }
        ) {
            androidx.compose.material3.Text("Start as Main Device")
        }

        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))

        androidx.compose.material3.Button(
            onClick = {
                startCommandListener()
                navController.navigate("speaker_device")
            }
        ) {
            androidx.compose.material3.Text("Start as Speaker Device")
        }
    }
}

//fun startBroadcasting() {
//    Log.d("Broadcasting", "Starting Broadcasting Thread...")
//    Thread {
//        try {
//            val socket = DatagramSocket()
//            val group = InetAddress.getByName("224.0.0.1") // Multicast group IP
//            val message = "FIVEPOINTONE_DISCOVERY".toByteArray()
//
//            while (true) {
//                val packet = DatagramPacket(message, message.size, group, 9876)
//                socket.send(packet)
//                Log.d("Broadcasting", "Broadcasted message to $group:9876")
//                Thread.sleep(2000)
//            }
//        } catch (e: Exception) {
//            Log.e("Broadcasting", "Error broadcasting", e)
//        }
//    }.start()
//}

//fun startBroadcasting(): AtomicBoolean {
//    Log.d("Broadcasting", "Starting Broadcasting Thread...")
//    var socket: DatagramSocket? = null
//    val isBroadcasting = AtomicBoolean(true)
//
//    Thread {
//        try {
//            socket = DatagramSocket()
//            socket!!.broadcast = true
//            val group = InetAddress.getByName("224.0.0.1") // Multicast group IP
//            val message = "FIVEPOINTONE_DISCOVERY".toByteArray()
//
//            while (isBroadcasting.get()) {
//                val packet = DatagramPacket(message, message.size, group, 9876)
//                socket!!.send(packet)
//                Log.d("Broadcasting", "Broadcasted discovery message to $group:9876")
//                Thread.sleep(2000)
//            }
//        } catch (e: Exception) {
//            Log.e("Broadcasting", "Error broadcasting", e)
//        } finally {
//            socket?.close()
//        }
//    }.start()
//
//    // Return the control object to stop broadcasting when needed
//    return isBroadcasting
//}


fun startBroadcasting(): AtomicBoolean {
    Log.d("Broadcasting", "Starting Broadcasting Thread...")
    var socket: DatagramSocket? = null
    val isBroadcasting = AtomicBoolean(true)

    Thread {
        try {
            Log.d("Broadcasting", "Creating DatagramSocket")
            socket = DatagramSocket()
            socket!!.broadcast = true

            val group = InetAddress.getByName("224.0.0.1") // Multicast group IP
            val message = "FIVEPOINTONE_DISCOVERY".toByteArray()

            Log.d("Broadcasting", "Will broadcast to group $group on port 9876")

            // Log active network interfaces for debugging
            logNetworkInterfaces()

            var count = 0
            while (isBroadcasting.get()) {
                val packet = DatagramPacket(message, message.size, group, 9876)
                socket!!.send(packet)
                count++
                Log.d("Broadcasting", "Broadcast #$count: Sent discovery message to $group:9876")
                Thread.sleep(2000)
            }
        } catch (e: Exception) {
            Log.e("Broadcasting", "Error broadcasting", e)
        } finally {
            Log.d("Broadcasting", "Closing broadcast socket")
            socket?.close()
        }
    }.start()

    return isBroadcasting
}

// Helper function to log all network interfaces and their IP addresses
private fun logNetworkInterfaces() {
    try {
        Log.d("NetworkInfo", "--- Available Network Interfaces ---")
        val interfaces = NetworkInterface.getNetworkInterfaces()
        while (interfaces.hasMoreElements()) {
            val networkInterface = interfaces.nextElement()

            if (!networkInterface.isUp) continue

            Log.d("NetworkInfo", "Interface: ${networkInterface.displayName} (${networkInterface.name})")

            val addresses = networkInterface.inetAddresses
            while (addresses.hasMoreElements()) {
                val address = addresses.nextElement()
                if (!address.isLoopbackAddress) {
                    val addressType = if (address is Inet4Address) "IPv4" else "IPv6"
                    Log.d("NetworkInfo", "  $addressType: ${address.hostAddress}")
                }
            }
        }
        Log.d("NetworkInfo", "------------------------------------")
    } catch (e: Exception) {
        Log.e("NetworkInfo", "Error logging network interfaces", e)
    }
}