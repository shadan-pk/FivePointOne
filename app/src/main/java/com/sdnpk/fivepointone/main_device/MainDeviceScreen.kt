package com.sdnpk.fivepointone.main_device

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
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
import com.sdnpk.fivepointone.utils.startMulticastSender
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sdnpk.fivepointone.speaker_device.connection.SpeakerListener


@Composable
fun MainDeviceScreen(
    navController: NavController,
    viewModel: MainDeviceViewModel = viewModel()
) {
    val discoveredSpeakers by viewModel.discoveredSpeakers.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var deviceIp by remember { mutableStateOf("Fetching...") }
    var connectionRequestedFrom by remember { mutableStateOf<String?>(null) }


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

    val speakerListener = remember {
        SpeakerListener(
            port = 6000,
            deviceId = "MainDeviceID", // Replace with your actual device ID logic
            onConnectionRequest = { speakerIp ->
                connectionRequestedFrom = speakerIp
                // Optional: update speaker UI state or log
            },
            onAcceptConnectionSignal = {

            }
        )
    }

    // 4. Start UDP listener
    LaunchedEffect(Unit) {
        speakerListener.startListening(coroutineScope)
    }

    // 5. Stop listener when screen is removed
    DisposableEffect(Unit) {
        onDispose {
            speakerListener.stopListening()
        }
    }



    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Main Device Screen", style = MaterialTheme.typography.headlineMedium)
        Text("Device IP: $deviceIp", style = MaterialTheme.typography.bodyLarge)

        Text("Discovered Speakers:", style = MaterialTheme.typography.titleMedium)
//        LazyColumn {
//            items(
//                items = discoveredSpeakers,
//                key = { speaker -> speaker.id }
//            ) { speaker ->
//                SpeakerCard(speaker)
//            }
//        }
        LazyColumn {
            items(discoveredSpeakers, key = { it.id }) { speaker ->
                SpeakerCard(
                    speaker = speaker,
                    onConnectClick = { connectToSpeaker(it) },
                    onRoleSelected = { device, role -> assignRoleToSpeaker(device, role) }
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


