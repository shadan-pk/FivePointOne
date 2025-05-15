package com.sdnpk.fivepointone.main_device

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Your project imports:
import com.sdnpk.fivepointone.data.SpeakerDevice
import com.sdnpk.fivepointone.main_device.MainDeviceViewModel
import androidx.lifecycle.viewmodel.compose.viewModel


@Composable
fun SpeakerCard(speaker: SpeakerDevice) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("ID: ${speaker.id}")
            Text("IP: ${speaker.ip}")
            Text("BT Connected: ${speaker.bluetoothConnected}")
            Text("Latency: ${speaker.latencyMs} ms")
            Text("Assigned Role: ${speaker.assignedRole?.name ?: "None"}")
        }
    }
}
