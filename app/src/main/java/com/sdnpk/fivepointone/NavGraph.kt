package com.sdnpk.fivepointone

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

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