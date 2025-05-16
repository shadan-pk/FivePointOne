package com.sdnpk.fivepointone.main_device

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import android.os.Build
import androidx.navigation.NavHostController
import com.sdnpk.fivepointone.ui.SpeakerDeviceScreen


@Composable
fun SpeakerScreenWrapper(navController: NavController) {
    val isBroadcasting = remember { mutableStateOf(false) }
    val connectionRequested = remember { mutableStateOf(false) }
    val mainDeviceIp = remember { mutableStateOf<String?>(null) }

    SpeakerDeviceScreen(
        deviceId = "DEVICE_${Build.MODEL}",
        isBluetoothConnected = false,
        isUsingPhoneSpeaker = true,
        isBroadcasting = isBroadcasting,
        mainDeviceIp = mainDeviceIp.value,
        connectionRequested = connectionRequested.value,
        onAcceptConnection = {
            // e.g., handle ACK here
            connectionRequested.value = false
        },
        navController = navController as NavHostController
    )
}
