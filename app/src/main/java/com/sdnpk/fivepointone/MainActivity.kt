    package com.sdnpk.fivepointone

    import android.os.Bundle
    import androidx.activity.ComponentActivity
    import androidx.activity.compose.setContent
    import androidx.activity.enableEdgeToEdge
    import androidx.compose.foundation.layout.fillMaxSize
    import androidx.compose.foundation.layout.padding
    import androidx.compose.material3.*
    import androidx.compose.runtime.Composable
    import androidx.compose.ui.Modifier
    import androidx.navigation.NavHostController
    import androidx.navigation.compose.NavHost
    import androidx.navigation.compose.composable
    import androidx.navigation.compose.rememberNavController
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
                    MainDeviceScreen(navController)
                }
                composable("speaker_device") {
                    SpeakerDeviceScreen(navController)  // Pass navController here for SpeakerDeviceScreen
                }
                // Note: Keeping this route for compatibility, but it won't be used anymore
                composable("load_music") {
                    MainDeviceScreen(navController)  // Redirect back to MainDeviceScreen
                }
            }
        }
    }
