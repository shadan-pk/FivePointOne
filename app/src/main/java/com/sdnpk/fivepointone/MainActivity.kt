package com.sdnpk.fivepointone

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
//import com.sdnpk.fivepointone.home.HomeScreen
import com.sdnpk.fivepointone.media.MediaPlayerManager
import com.sdnpk.fivepointone.network.SpeakerNetworkManager

/**
 * Main navigation component that defines the app's navigation structure
 */
@Composable
fun AppNavigation() {
    val navController: NavHostController = rememberNavController()

    // Create managers that need to persist across navigation
    val mediaPlayerManager = MediaPlayerManager(androidx.compose.ui.platform.LocalContext.current)
    val speakerNetworkManager = SpeakerNetworkManager()

    // Start speaker discovery service
    androidx.compose.runtime.LaunchedEffect(Unit) {
        speakerNetworkManager.startSpeakerDiscovery()
    }

    // Clean up resources when destroyed
    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose {
            mediaPlayerManager.releaseResources()
            speakerNetworkManager.stopSpeakerDiscovery()
        }
    }

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
                    navController = navController,
                    mediaPlayerManager = mediaPlayerManager,
                    speakerNetworkManager = speakerNetworkManager
                )
            }

            composable("speaker_device") {
                SpeakerDeviceScreen(
                    navController = navController,
                    speakerRole = "RearLeft", // This could be configurable in settings
                    mainDeviceIp = null // This will be detected by the screen
                )
            }

            // Note: Keeping this route for compatibility, but it redirects to main_device
            composable("load_music") {
                MainDeviceScreen(
                    navController = navController,
                    mediaPlayerManager = mediaPlayerManager,
                    speakerNetworkManager = speakerNetworkManager
                )
            }
        }
    }
}