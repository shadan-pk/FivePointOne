package com.sdnpk.fivepointone.main_device

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import kotlinx.coroutines.delay
import com.sdnpk.fivepointone.utils.startMulticastSender
import com.sdnpk.fivepointone.main_device.connection.sendConnectRequest


@Composable
fun MainDeviceScreen(
    navController: NavController,
//    viewModel: MainDeviceViewModel = viewModel(),
    viewModel: MainDeviceViewModel

) {
    val discoveredSpeakers by viewModel.discoveredSpeakers.collectAsState()
    var showDisconnectDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var deviceIp by remember { mutableStateOf("Fetching...") }

    // Get device IP
    LaunchedEffect(Unit) {
        deviceIp = getDeviceIpAddress(context)
    }

    // Request multicast permission
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (!granted) {
                Toast.makeText(context, "Multicast permission denied", Toast.LENGTH_LONG).show()
            }
        }
    )

    BackHandler {
        showDisconnectDialog = true
    }

    if (showDisconnectDialog) {
        AlertDialog(
            onDismissRequest = { showDisconnectDialog = false },
            title = { Text("Disconnect") },
            text = { Text("Do you want to disconnect and leave the screen?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.disconnectAllSpeakers()
                    showDisconnectDialog = false
                    navController.popBackStack() // Navigate back here
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDisconnectDialog = false }) {
                    Text("No")
                }
            }
        )
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(3000)
            viewModel.checkInactiveSpeakers()
        }
    }



    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CHANGE_WIFI_MULTICAST_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(Manifest.permission.CHANGE_WIFI_MULTICAST_STATE)
        }

        // Start receiving speaker broadcasts
        startSpeakerDiscoveryReceiver(viewModel)

        // Start multicast sender
        startMulticastSender()

    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Main Device Screen", style = MaterialTheme.typography.headlineMedium)
        Text("Device IP: $deviceIp", style = MaterialTheme.typography.bodyLarge)

        Text("Discovered Speakers:", style = MaterialTheme.typography.titleMedium)
        LazyColumn {
            items(discoveredSpeakers, key = { it.id }) { speaker ->
                SpeakerCard(
                    speaker = speaker,
                    onConnectClick = { speaker ->
                        sendConnectRequest(speaker) { success ->
                            if (success) {
                                Log.d("MainDeviceScreen", "Successfully connected to ${speaker.id}")
                                viewModel.markSpeakerAsConnected(speaker.id)
                            } else {
                                Log.e("MainDeviceScreen", "Connection failed to ${speaker.id}")
                            }
                        }
                    },
                    onConfigClick = { speaker ->
                        navController.navigate("speakerConfig/${speaker.id}")
                    }
                )
            }
        }
        Button(onClick = {
            navController.navigate("mediaPlayback")
        }) {
            Text("Go to Media Playback")
        }
    }
}


