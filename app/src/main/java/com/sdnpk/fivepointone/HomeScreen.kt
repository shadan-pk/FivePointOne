package com.sdnpk.fivepointone

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Build
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.Inet4Address
import java.net.NetworkInterface

@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    var hasNetworkPermissions by remember {
        mutableStateOf(
            hasRequiredPermissions(context)
        )
    }

    // Request required permissions
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            val allGranted = permissions.all { it.value }
            hasNetworkPermissions = allGranted
            if (!allGranted) {
                Toast.makeText(context, "Network permissions are required for proper functionality", Toast.LENGTH_LONG).show()
            }
        }
    )

    LaunchedEffect(true) {
        if (!hasNetworkPermissions) {
            requestRequiredPermissions(permissionLauncher)
        }
    }

    // Manage multicast lock
    val wifiManager = remember { context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager }
    val multicastLock = remember { wifiManager.createMulticastLock("fivepointone_multicast_lock") }

    DisposableEffect(Unit) {
        if (!multicastLock.isHeld) {
            multicastLock.setReferenceCounted(true)
            multicastLock.acquire()
        }
        onDispose {
            if (multicastLock.isHeld) {
                multicastLock.release()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("FivePointOne", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        // Display local IP address
        val localIp = remember { getLocalIpAddress() }
        Text("Your device IP: $localIp", style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = {
            if (hasNetworkPermissions) {
                startBroadcasting(context)
                navController.navigate("main_device")
            } else {
                Toast.makeText(context, "Please grant network permissions", Toast.LENGTH_SHORT).show()
                requestRequiredPermissions(permissionLauncher)
            }
        }) {
            Text("Act as Main Device")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            if (hasNetworkPermissions) {
                navController.navigate("speaker_device")
            } else {
                Toast.makeText(context, "Please grant network permissions", Toast.LENGTH_SHORT).show()
                requestRequiredPermissions(permissionLauncher)
            }
        }) {
            Text("Act as Speaker Device")
        }
    }
}

fun startBroadcasting(context: Context) {
    Log.d("Broadcasting", "Starting Broadcasting Thread...")

    // Get the local IP address for logging
    val localIp = getLocalIpAddress()
    Log.d("Broadcasting", "Broadcasting from IP: $localIp")

    // Acquire multicast lock
    val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    val multicastLock = wifiManager.createMulticastLock("broadcast_lock")
    multicastLock.setReferenceCounted(true)
    multicastLock.acquire()

    CoroutineScope(Dispatchers.IO).launch {
        var socket: DatagramSocket? = null
        try {
            socket = DatagramSocket()
            socket.broadcast = true

            // Use both multicast and broadcast addresses for better discovery
            val multicastGroup = InetAddress.getByName("224.0.0.1")
            val broadcastAddress = InetAddress.getByName("255.255.255.255")

            // Include the local IP in the message for better identification
            val message = "FIVEPOINTONE_DISCOVERY:$localIp".toByteArray()

            var count = 0
            while (count < 300) { // Run for approximately 10 minutes (2s * 300)
                // Send multicast packet
                val multicastPacket = DatagramPacket(message, message.size, multicastGroup, 9876)
                socket.send(multicastPacket)
                Log.d("Broadcasting", "Broadcasted multicast message to 224.0.0.1:9876")

                // Send broadcast packet
                val broadcastPacket = DatagramPacket(message, message.size, broadcastAddress, 9876)
                socket.send(broadcastPacket)
                Log.d("Broadcasting", "Broadcasted message to 255.255.255.255:9876")

                Thread.sleep(2000)
                count++
            }
        } catch (e: Exception) {
            Log.e("Broadcasting", "Error broadcasting", e)
        } finally {
            socket?.close()
            if (multicastLock.isHeld) {
                multicastLock.release()
            }
        }
    }
}

fun getLocalIpAddress(): String {
    try {
        val interfaces = NetworkInterface.getNetworkInterfaces()
        while (interfaces.hasMoreElements()) {
            val networkInterface = interfaces.nextElement()

            // Skip loopback and inactive interfaces
            if (!networkInterface.isUp || networkInterface.isLoopback) {
                continue
            }

            val addresses = networkInterface.inetAddresses
            while (addresses.hasMoreElements()) {
                val address = addresses.nextElement()
                if (!address.isLoopbackAddress && address is Inet4Address) {
                    return address.hostAddress
                }
            }
        }
    } catch (e: Exception) {
        Log.e("IP Address", "Error getting local IP address", e)
    }
    return "Unknown"
}

fun hasRequiredPermissions(context: Context): Boolean {
    val requiredPermissions = mutableListOf<String>()

    // Add network permissions
    requiredPermissions.add(Manifest.permission.INTERNET)
    requiredPermissions.add(Manifest.permission.ACCESS_NETWORK_STATE)
    requiredPermissions.add(Manifest.permission.ACCESS_WIFI_STATE)
    requiredPermissions.add(Manifest.permission.CHANGE_WIFI_MULTICAST_STATE)

    // For Android 10+, we may need fine location for WiFi operations
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        requiredPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    return requiredPermissions.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }
}

fun requestRequiredPermissions(permissionLauncher: androidx.activity.result.ActivityResultLauncher<Array<String>>) {
    val requiredPermissions = mutableListOf<String>()

    // Add network permissions
    requiredPermissions.add(Manifest.permission.INTERNET)
    requiredPermissions.add(Manifest.permission.ACCESS_NETWORK_STATE)
    requiredPermissions.add(Manifest.permission.ACCESS_WIFI_STATE)
    requiredPermissions.add(Manifest.permission.CHANGE_WIFI_MULTICAST_STATE)

    // For Android 10+, we may need fine location for WiFi operations
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        requiredPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    permissionLauncher.launch(requiredPermissions.toTypedArray())
}