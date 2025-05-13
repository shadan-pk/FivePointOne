package com.sdnpk.fivepointone

import android.Manifest
import android.content.pm.PackageManager
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

@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    var hasMulticastPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CHANGE_WIFI_MULTICAST_STATE
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Request multicast permission
    val wifiMulticastPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            hasMulticastPermission = isGranted
            if (!isGranted) {
                Toast.makeText(context, "Multicast permission denied", Toast.LENGTH_LONG).show()
            }
        }
    )

    LaunchedEffect(true) {
        if (!hasMulticastPermission) {
            wifiMulticastPermissionLauncher.launch(Manifest.permission.CHANGE_WIFI_MULTICAST_STATE)
        }
    }

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
            if (hasMulticastPermission) {
                navController.navigate("main_device")
            } else {
                Toast.makeText(context, "Please grant multicast permission", Toast.LENGTH_SHORT).show()
            }
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
            socket.broadcast = true // Enable broadcast
            val group = InetAddress.getByName("224.0.0.1")
            val message = "FIVEPOINTONE_DISCOVERY".toByteArray()
            while (!Thread.currentThread().isInterrupted) {
                val packet = DatagramPacket(message, message.size, group, 9876)
                socket.send(packet)
                Log.d("Broadcasting", "Broadcasted message to $group:9876")
                Thread.sleep(2000)
            }
            socket.close()
        } catch (e: Exception) {
            Log.e("Broadcasting", "Error broadcasting", e)
        }
    }.start()
}