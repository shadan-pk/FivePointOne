package com.sdnpk.fivepointone

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ConnectedSpeakersList(connectedSpeakers: List<String>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Connected Speakers:", style = MaterialTheme.typography.headlineSmall)

        if (connectedSpeakers.isEmpty()) {
            Text("No speakers connected", style = MaterialTheme.typography.bodyMedium)
        } else {
            connectedSpeakers.forEach { speakerId ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Speaker ID: $speakerId", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }
}
