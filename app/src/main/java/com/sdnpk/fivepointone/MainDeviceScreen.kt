package com.sdnpk.fivepointone

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
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

@Composable
fun MainDeviceScreen(navController: NavController) {
    val context = LocalContext.current
    var connectedSpeakers by remember { mutableStateOf<List<String>>(emptyList()) }
    val broadcastThread = remember { mutableStateOf<Thread?>(null) }

    // Start listening for speaker responses
    LaunchedEffect(true) {
        listenForSpeakerResponses { speakerIp ->
            connectedSpeakers = (connectedSpeakers + speakerIp).distinct()
        }
    }

    // Request multicast permission
    val wifiMulticastPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (!isGranted) {
                Toast.makeText(context, "Multicast permission denied", Toast.LENGTH_LONG).show()
            }
        }
    )

    LaunchedEffect(true) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CHANGE_WIFI_MULTICAST_STATE) != PackageManager.PERMISSION_GRANTED) {
            wifiMulticastPermissionLauncher.launch(Manifest.permission.CHANGE_WIFI_MULTICAST_STATE)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            broadcastThread.value?.interrupt()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Main Device Screen", style = MaterialTheme.typography.headlineMedium)
        ConnectedSpeakersList(connectedSpeakers = connectedSpeakers)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            navController.navigate("mediaPlayback")
        }) {
            Text("Go to Media Playback")
        }
    }
}