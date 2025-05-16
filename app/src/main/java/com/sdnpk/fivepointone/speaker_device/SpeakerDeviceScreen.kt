// ui/SpeakerDeviceScreen.kt
package com.sdnpk.fivepointone.ui

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
//import com.sdnpk.fivepointone.speaker_device.SpeakerBroadcaster
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.sdnpk.fivepointone.speaker_device.SpeakerBroadcaster
import com.sdnpk.fivepointone.speaker_device.DiscoveryViewModel
import com.sdnpk.fivepointone.speaker_device.connection.SpeakerUnicastListener
import com.sdnpk.fivepointone.utils.startMulticastReceiver
import androidx.compose.runtime.getValue


@Composable
fun SpeakerDeviceScreen(
    deviceId: String,
    isBluetoothConnected: Boolean,
    isUsingPhoneSpeaker: Boolean,
    navController: NavController, // <-- add navController
    viewModel: DiscoveryViewModel = viewModel() // <-- use ViewModel
) {
    val context = LocalContext.current
    val isBroadcasting = remember { mutableStateOf(false) }
//    val mainDeviceIp = viewModel.mainDeviceIp.value
    val mainDeviceIp by viewModel.mainDeviceIp


//    val mainDeviceIp = viewModel.mainDeviceIp.collectAsState().value

    val multicastPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            Toast.makeText(context, "Multicast permission denied", Toast.LENGTH_LONG).show()
        }
    }

    val coroutineScope = rememberCoroutineScope()
    var connectionRequested by remember { mutableStateOf(false) }

//    var mainDeviceIp by remember { mutableStateOf<String?>(null) }

    val speakerListener = remember {
        SpeakerUnicastListener(
            port = 6000,
            deviceId = "DEVICE_${Build.MODEL}",
            onConnectRequestReceived = { ip ->
                connectionRequested = true  // local mutable state
                viewModel.setMainDeviceIp(ip)  // update ViewModel state via method
            }
        )
    }

    LaunchedEffect(Unit) {
        speakerListener.start(coroutineScope)
    }

    DisposableEffect(Unit) {
        onDispose {
            speakerListener.stop()
        }
    }

    // Start listening to main device broadcasts
    LaunchedEffect(Unit) {
        startMulticastReceiver(context) { ip, msg ->
            Log.d("MulticastReceiver", "Main device detected: $ip")
            viewModel.setMainDeviceIp(ip)
        }
    }

    // Request permission and start broadcasting this speaker's info
    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CHANGE_WIFI_MULTICAST_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            multicastPermissionLauncher.launch(Manifest.permission.CHANGE_WIFI_MULTICAST_STATE)
        } else {
            startBroadcast(context, deviceId, isBluetoothConnected, isUsingPhoneSpeaker)
            isBroadcasting.value = true
        }
    }

    // Stop broadcasting on dispose
    DisposableEffect(Unit) {
        onDispose {
            SpeakerBroadcaster.stopBroadcasting()
        }
    }

    // UI
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Speaker Device Mode", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        Text("Device ID: $deviceId")
        Text("Bluetooth: $isBluetoothConnected")
        Text("Using Phone Speaker: $isUsingPhoneSpeaker")
        Text("Status: ${if (isBroadcasting.value) "Broadcasting" else "Idle"}")
        Spacer(modifier = Modifier.height(16.dp))
        Text("Main IP: ${mainDeviceIp ?: "Waiting..."}")

        // ✅ Show button only when a connect_request was received
        if (connectionRequested) {
            Spacer(modifier = Modifier.height(24.dp))
            Text("Connection request from main device!")
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = {
                speakerListener.acceptConnection()
                connectionRequested = false
            }) {
                Text("Accept Connection")
            }
        }
    }

}

private fun startBroadcast(
    context: Context,
    deviceId: String,
    isBluetoothConnected: Boolean,
    isUsingPhoneSpeaker: Boolean
) {
    SpeakerBroadcaster.startBroadcasting(deviceId, isBluetoothConnected, isUsingPhoneSpeaker)
}

