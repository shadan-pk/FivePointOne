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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import com.sdnpk.fivepointone.utils.startMulticastSender
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sdnpk.fivepointone.utils.startMulticastReceiver
import kotlinx.coroutines.flow.forEach


@Composable
fun MainDeviceScreen(navController: NavController, viewModel: MainDeviceViewModel = viewModel()) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val speakers by viewModel.discoveredSpeakers.collectAsState()

    var deviceIp by remember { mutableStateOf("Fetching IP...") }
    val deviceIpService = remember { DeviceIpService(context) }

    LaunchedEffect(Unit) {
        startMulticastSender()
    }

    LaunchedEffect(true) {
        deviceIp = deviceIpService.getDeviceIpAddress()
        DiscoveryListenerService.startListening()

        // Start discovery listener
        scope.launch {
            while (true) {
                delay(1000) // Update every second, adjust based on needs
            }
        }
    }

    val wifiMulticastPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (!isGranted) {
                Toast.makeText(context, "Multicast permission denied", Toast.LENGTH_LONG).show()
            }
        }
    )

    LaunchedEffect(true) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CHANGE_WIFI_MULTICAST_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            wifiMulticastPermissionLauncher.launch(Manifest.permission.CHANGE_WIFI_MULTICAST_STATE)
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

        Text("Main Device IP: $deviceIp", style = MaterialTheme.typography.bodyLarge)

        Text("Discovered Speakers:", style = MaterialTheme.typography.bodyLarge)

        // Display discovered speakers
        speakers.forEach { speaker ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                elevation = CardDefaults.cardElevation()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("ID: ${speaker.getString("id")}")
                    Text("Bluetooth: ${speaker.getBoolean("bluetooth_connected")}")
                    Text("Phone Speaker: ${speaker.getBoolean("is_phone_speaker")}")
                    Text("Timestamp: ${speaker.getLong("timestamp")}")
                }
            }
        }

        Button(onClick = {
            navController.navigate("mediaPlayback")
        }) {
            Text("Go to Media Playback")
        }
    }
}

