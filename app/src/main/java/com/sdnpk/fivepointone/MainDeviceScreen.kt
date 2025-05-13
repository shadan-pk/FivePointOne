package com.sdnpk.fivepointone

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import com.sdnpk.fivepointone.media.MediaPlayerManager
import com.sdnpk.fivepointone.media.MediaPlayerUI
import com.sdnpk.fivepointone.network.ConnectedSpeakersList
import com.sdnpk.fivepointone.network.SpeakerNetworkManager
import com.sdnpk.fivepointone.ui.theme.FivePointOneTheme
import kotlinx.coroutines.launch

class MainDeviceActivity : ComponentActivity() {
    // Create instances that need to persist across recompositions
    private val speakerNetworkManager = SpeakerNetworkManager()
    private lateinit var mediaPlayerManager: MediaPlayerManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the media player manager
        mediaPlayerManager = MediaPlayerManager(this)

        // Start speaker discovery
        speakerNetworkManager.startSpeakerDiscovery()

        setContent {
            FivePointOneTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainDeviceScreen(
                        navController = NavController(this),
                        mediaPlayerManager = mediaPlayerManager,
                        speakerNetworkManager = speakerNetworkManager
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        // Clean up resources
        mediaPlayerManager.releaseResources()
        speakerNetworkManager.stopSpeakerDiscovery()
        super.onDestroy()
    }
}

@Composable
fun MainDeviceScreen(
    navController: NavController,
    mediaPlayerManager: MediaPlayerManager,
    speakerNetworkManager: SpeakerNetworkManager
) {
    val context = LocalContext.current
    val connectedSpeakers by speakerNetworkManager.connectedSpeakers.collectAsState()
    val mediaState by mediaPlayerManager.mediaState.collectAsState()

    // Permission state
    var hasPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_MEDIA_AUDIO
                ) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.READ_MEDIA_VIDEO
                        ) == PackageManager.PERMISSION_GRANTED
            } else {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            }
        )
    }

    // File picker launcher
    val openFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                // Get file name and determine if it's video
                context.contentResolver.query(it, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    cursor.moveToFirst()
                    val fileName = cursor.getString(nameIndex)

                    // Check if it's a video file by mime type
                    val mimeType = context.contentResolver.getType(it)
                    val isVideo = mimeType?.startsWith("video/") == true

                    // Set the media source in our manager
                    mediaPlayerManager.setMediaSource(it, fileName, isVideo)

                    // Sync with any connected speakers when media changes
                    if (connectedSpeakers.isNotEmpty()) {
                        speakerNetworkManager.sendPlaybackCommand("MEDIA_LOAD|${fileName}")
                    }
                }
            }
        }
    )

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            val allGranted = permissions.all { it.value }
            hasPermission = allGranted
            if (!allGranted) {
                Toast.makeText(
                    context,
                    "Storage permissions denied. Cannot access media files.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    )

    // Observe media playback changes to sync with speakers
    LaunchedEffect(mediaState.isPlaying) {
        if (connectedSpeakers.isNotEmpty()) {
            val command = if (mediaState.isPlaying) "PLAY" else "PAUSE"
            speakerNetworkManager.sendPlaybackCommand(command)
        }
    }

    // Main screen layout
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Main Device Control Center", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        // Show connected speakers
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            ConnectedSpeakersList(
                connectedSpeakers = connectedSpeakers,
                modifier = Modifier.padding(16.dp)
            )
        }

        // Media player section
        MediaPlayerUI(
            mediaPlayerManager = mediaPlayerManager,
            onLoadAudio = {
                checkAndRequestPermissionsThenLoad(
                    context = context,
                    hasPermission = hasPermission,
                    permissionLauncher = permissionLauncher,
                    onPermissionGranted = { openFileLauncher.launch("audio/*") }
                )
            },
            onLoadVideo = {
                checkAndRequestPermissionsThenLoad(
                    context = context,
                    hasPermission = hasPermission,
                    permissionLauncher = permissionLauncher,
                    onPermissionGranted = { openFileLauncher.launch("video/*") }
                )
            }
        )
    }
}

/**
 * Helper function to check and request permissions before loading media
 */
private fun checkAndRequestPermissionsThenLoad(
    context: android.content.Context,
    hasPermission: Boolean,
    permissionLauncher: androidx.activity.result.ActivityResultLauncher<Array<String>>,
    onPermissionGranted: () -> Unit
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED
        ) {
            onPermissionGranted()
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_MEDIA_AUDIO,
                    Manifest.permission.READ_MEDIA_VIDEO
                )
            )
        }
    } else {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            onPermissionGranted()
        } else {
            permissionLauncher.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
        }
    }
}