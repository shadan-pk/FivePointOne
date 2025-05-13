package com.sdnpk.fivepointone.main_device

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sdnpk.fivepointone.MediaPlaybackScreen

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "mainDevice") {
        composable("mainDevice") {
            MainDeviceScreen(navController)
        }
        composable("mediaPlayback") {
            MediaPlaybackScreen(navController)
        }
    }
}