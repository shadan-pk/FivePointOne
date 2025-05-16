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
import android.util.Log
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.sdnpk.fivepointone.speaker_device.SpeakerBroadcaster
import com.sdnpk.fivepointone.speaker_device.DiscoveryViewModel
import com.sdnpk.fivepointone.utils.startMulticastReceiver

@Composable
fun SpeakerDeviceScreen(
    deviceId: String,
    isBluetoothConnected: Boolean,
    isUsingPhoneSpeaker: Boolean,
    viewModel: DiscoveryViewModel = viewModel(), // <-- use ViewModel

    isBroadcasting: MutableState<Boolean>,
    mainDeviceIp: String?,
    onAcceptConnection: () -> Unit,
    connectionRequested: Boolean,
    navController: NavHostController
) {
    val context = LocalContext.current
    val isBroadcasting = remember { mutableStateOf(false) }
    val mainDeviceIp = viewModel.mainDeviceIp.value

    val multicastPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            Toast.makeText(context, "Multicast permission denied", Toast.LENGTH_LONG).show()
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
//    Column(
//        modifier = Modifier.fillMaxSize().padding(24.dp),
//        verticalArrangement = Arrangement.Center,
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Text("Speaker Device Mode", style = MaterialTheme.typography.headlineMedium)
//        Spacer(modifier = Modifier.height(16.dp))
//
//        Text("Device ID: $deviceId")
//        Text("Bluetooth: $isBluetoothConnected")
//        Text("Using Phone Speaker: $isUsingPhoneSpeaker")
//        Text("Status: ${if (isBroadcasting.value) "Broadcasting" else "Idle"}")
//        Spacer(modifier = Modifier.height(16.dp))
//        Text("Main IP: ${mainDeviceIp ?: "Waiting..."}")
//    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
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
        Spacer(modifier = Modifier.height(24.dp))

        if (connectionRequested) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Connection request received from main device!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = onAcceptConnection) {
                        Text("Accept Connection")
                    }
                }
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

