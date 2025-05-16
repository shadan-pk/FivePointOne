package com.sdnpk.fivepointone.main_device

// Your project imports:

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sdnpk.fivepointone.config.Role
import com.sdnpk.fivepointone.data.SpeakerDevice


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeakerCard(
    speaker: SpeakerDevice,
    onConnectClick: (SpeakerDevice) -> Unit,
    onRoleSelected: (SpeakerDevice, Role) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

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
            Text("Latency: ${speaker.latencyMs?.toString() ?: "—"} ms")
            Text("Assigned Role: ${speaker.assignedRole?.name ?: "None"}")

            Spacer(Modifier.height(8.dp))

            if (!speaker.connected) {
                Button(onClick = { onConnectClick(speaker) }) {
                    Text("Connect")
                }
            } else {

                // Role dropdown only shown after connection
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    TextField(
                        readOnly = true,
                        value = speaker.assignedRole?.name ?: "Select Role",
                        onValueChange = {},
                        label = { Text("Role") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        colors = ExposedDropdownMenuDefaults.textFieldColors()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        Role.entries.forEach { role ->
                            DropdownMenuItem(
                                text = { Text(role.name) },
                                onClick = {
                                    expanded = false
                                    onRoleSelected(speaker, role)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

