package com.sdnpk.fivepointone

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sdnpk.fivepointone.main_device.MainDeviceScreen
import com.sdnpk.fivepointone.main_device.MainDeviceViewModel
import com.sdnpk.fivepointone.main_device.connection.sendPingRequest
import com.sdnpk.fivepointone.main_device.screen.SpeakerConfigScreen
import com.sdnpk.fivepointone.ui.SpeakerDeviceScreen
import com.sdnpk.fivepointone.ui.theme.FivePointOneTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FivePointOneTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController: NavHostController = rememberNavController()
    val mainDeviceViewModel: MainDeviceViewModel = viewModel()

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                HomeScreen(navController)
            }
            composable("main_device") {
                MainDeviceScreen(
                    viewModel = mainDeviceViewModel,
                    navController = navController
                )
            }
            composable("speaker_device") {
                SpeakerDeviceScreen(
                    deviceId = "DEVICE_${Build.MODEL}",
                    isBluetoothConnected = false,
                    isUsingPhoneSpeaker = true,
                    navController = navController // <-- pass this
                )
            }

            composable("speakerConfig/{speakerId}") { backStackEntry ->
                val speakerId = backStackEntry.arguments?.getString("speakerId") ?: ""
                val speaker = mainDeviceViewModel.getSpeakerById(speakerId)

                if (speaker != null) {
                    SpeakerConfigScreen(
                        speaker = speaker,
                        sendPingRequest = { spk, callback -> sendPingRequest(spk, callback) }
                    )
                } else {
                    Text("Speaker not found")
                }
            }
            
            composable("mediaPlayback") {
                MediaPlaybackScreen(navController)
            }
            // Redirect legacy route to mediaPlayback for backward compatibility
            composable("load_music") {
                MediaPlaybackScreen(navController)
            }
        }
    }
}