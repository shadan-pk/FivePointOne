package com.sdnpk.fivepointone.main_device.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.sdnpk.fivepointone.data.SpeakerDevice

@Composable
fun SpeakerConfigScreen(
    speaker: SpeakerDevice,
    sendPingRequest: (SpeakerDevice, (Long?) -> Unit) -> Unit
) {
    var pingLatency by remember { mutableStateOf<Long?>(null) }
    var isPinging by remember { mutableStateOf(false) }
    val context = LocalContext.current


    // Helper to get color based on latency
    fun pingColor(latency: Long?): Color {
        return when {
            latency == null -> Color.Gray
            latency < 100 -> Color(0xFF4CAF50)       // Green
            latency < 300 -> Color(0xFFFFA000)       // Orange
            else -> Color(0xFFF44336)                 // Red
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Configuring Speaker: ${speaker.id}", style = MaterialTheme.typography.headlineSmall)

        Button(
            onClick = {
                isPinging = true
                sendPingRequest(speaker) { latency ->
                    pingLatency = latency
                    isPinging = false
                }
            },
            enabled = !isPinging
        ) {
            Text(if (isPinging) "Pinging..." else "Recalculate Ping")
        }

        pingLatency?.let {
            Text(
                text = "Ping: ${it} ms",
                color = pingColor(it),
                style = MaterialTheme.typography.titleMedium
            )
        } ?: Text(
            text = "Ping: --",
            color = Color.Gray,
            style = MaterialTheme.typography.titleMedium
        )
        Button(
            onClick = { streamTestAudioToSpeaker(context, speaker) }
        ) {
            Text("Test Sound Stream")
        }

        // Future config UI elements here
    }
}
