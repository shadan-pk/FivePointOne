package com.sdnpk.fivepointone.main_device

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Your project imports:
import com.sdnpk.fivepointone.data.SpeakerDevice
import com.sdnpk.fivepointone.main_device.MainDeviceViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

//@Composable
//fun SpeakerCard(
//    speaker: SpeakerDevice,
//    onConnectClick: (SpeakerDevice) -> Unit
//) {
//    val backgroundColor = if (speaker.isConnected) Color(0xFFE6F4EA) else Color(0xFFFFEBEE) // lighter shades
//    val textColor = if (speaker.isConnected) Color(0xFF1B5E20) else Color(0xFFB71C1C)         // darker greens and reds
//
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 4.dp),
//        colors = CardDefaults.cardColors(containerColor = backgroundColor),
//        elevation = CardDefaults.cardElevation()
//    ) {
//        Column(modifier = Modifier.padding(16.dp)) {
//            Text("ID: ${speaker.id}", color = textColor)
//            Text("IP: ${speaker.ip}", color = textColor)
//            Text("BT Connected: ${speaker.bluetoothConnected}", color = textColor)
//            Text("Latency: ${speaker.latencyMs} ms", color = textColor)
//            Text("Assigned Role: ${speaker.assignedRole?.name ?: "None"}", color = textColor)
//            Text("Status: ${if (speaker.isConnected) "Connected" else "Disconnected"}", color = textColor)
//
//            Spacer(modifier = Modifier.height(8.dp))
//
//            Button(
//                onClick = { onConnectClick(speaker) },
//                enabled = !speaker.isConnected
//            ) {
//                Text(if (speaker.isConnected) "Connected" else "Connect")
//            }
//        }
//    }
//
//}

@Composable
fun SpeakerCard(
    speaker: SpeakerDevice,
    onConnectClick: (SpeakerDevice) -> Unit
) {
    val backgroundColor = Color(0xFF2D2D2D) // dark black background
    val textColor = Color.White

    // Button colors based on connection status
    val buttonColors = when {
        speaker.isConnected -> ButtonDefaults.buttonColors(
            containerColor = Color(0xFF4CAF50),  // Green
            contentColor = Color.Black
        )
        !speaker.isConnected -> ButtonDefaults.buttonColors(
            containerColor = Color(0xFFF44336),  // Red
            contentColor = Color.White
        )
        else -> ButtonDefaults.buttonColors(
            containerColor = Color.Gray,          // Gray (fallback)
            contentColor = Color.White
        )
    }

    val statusText = if (speaker.isConnected) "Connected" else "Disconnected"
    val statusColor = if (speaker.isConnected) Color(0xFF4CAF50) else Color(0xFFF44336)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("ID: ${speaker.id}", color = textColor)
            Text("IP: ${speaker.ip}", color = textColor)
            Text("BT Connected: ${speaker.bluetoothConnected}", color = textColor)
            Text("Latency: ${speaker.latencyMs} ms", color = textColor)
            Text("Assigned Role: ${speaker.assignedRole?.name ?: "None"}", color = textColor)

            Spacer(modifier = Modifier.height(4.dp))

            Text("Status: $statusText", color = statusColor, style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { onConnectClick(speaker) },
                enabled = !speaker.isConnected,
                colors = buttonColors
            ) {
                Text(if (speaker.isConnected) "Connected" else "Connect")
            }
        }
    }
}


