package com.sdnpk.fivepointone

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.media.MediaPlayer
import android.os.Build
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

@Composable
fun ConnectedSpeakersList(connectedSpeakers: List<String>) {
    if (connectedSpeakers.isEmpty()) {
        Text(
            "No speakers connected yet",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
    } else {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "Connected Speakers (${connectedSpeakers.size})",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 150.dp)
                ) {
                    items(connectedSpeakers) { speaker ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .background(Color(0xFFEEEEEE))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(speaker, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MainDeviceScreen(navController: NavController) {
    val context = LocalContext.current
    val mediaUri = remember { mutableStateOf<Uri?>(null) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var videoView by remember { mutableStateOf<VideoView?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var fileName by remember { mutableStateOf("") }
    var isVideo by remember { mutableStateOf(false) }
    var connectedSpeakers by remember { mutableStateOf<List<String>>(emptyList()) }
    val localIp = remember { getLocalIpAddress() }

    // Manage multicast lock
    val wifiManager = remember { context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager }
    val multicastLock = remember { wifiManager.createMulticastLock("main_device_multicast_lock") }

    DisposableEffect(Unit) {
        if (!multicastLock.isHeld) {
            multicastLock.setReferenceCounted(true)
            multicastLock.acquire()
        }

        // Start broadcasting as soon as the screen loads
        startBroadcasting(context)

        onDispose {
            if (multicastLock.isHeld) {
                multicastLock.release()
            }
            mediaPlayer?.release()
            mediaPlayer = null
            videoView?.stopPlayback()
            videoView = null
        }
    }

    // Start listening for speaker responses
    LaunchedEffect(true) {
        listenForSpeakerResponses { speakerIp ->
            connectedSpeakers = (connectedSpeakers + speakerIp).distinct()
        }
    }

    // Permission state for media access
    var hasMediaPermission by remember {
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
                context.contentResolver.query(it, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    cursor.moveToFirst()
                    fileName = cursor.getString(nameIndex)
                    val mimeType = context.contentResolver.getType(it)
                    isVideo = mimeType?.startsWith("video/") == true
                }
                mediaPlayer?.release()
                mediaPlayer = null
                videoView?.stopPlayback()
                mediaUri.value = it
                if (!isVideo) {
                    mediaPlayer = MediaPlayer.create(context, it)
                    mediaPlayer?.setOnCompletionListener {
                        isPlaying = false
                    }
                }
            }
        }
    )

    // Permission launcher for media access
    val mediaPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            val allGranted = permissions.all { it.value }
            hasMediaPermission = allGranted
            if (allGranted) {
                openFileLauncher.launch("*/*")
            } else {
                Toast.makeText(
                    context,
                    "Storage permissions denied. Cannot access media files.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Main Device (IP: $localIp)",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Connected speakers list
        ConnectedSpeakersList(connectedSpeakers = connectedSpeakers)

        Spacer(modifier = Modifier.height(16.dp))

        // Media controls
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Media Player",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(onClick = {
                        requestMediaPermissionAndLaunch(context, mediaPermissionLauncher, openFileLauncher, "audio/*")
                    }) {
                        Text("Load Audio")
                    }

                    Button(onClick = {
                        requestMediaPermissionAndLaunch(context, mediaPermissionLauncher, openFileLauncher, "video/*")
                    }) {
                        Text("Load Video")
                    }
                }

                mediaUri.value?.let { uri ->
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Now Playing: $fileName",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    if (isVideo) {
                        Spacer(modifier = Modifier.height(16.dp))

                        AndroidView(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .border(1.dp, Color.Gray),
                            factory = { ctx ->
                                VideoView(ctx).apply {
                                    layoutParams = FrameLayout.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.MATCH_PARENT
                                    )
                                    setVideoURI(uri)
                                    setOnPreparedListener { mp ->
                                        mp.setOnCompletionListener {
                                            isPlaying = false
                                        }
                                    }
                                    videoView = this
                                }
                            },
                            update = { view ->
                                if (isPlaying) {
                                    view.start()
                                } else {
                                    view.pause()
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(onClick = {
                            if (isPlaying) {
                                if (isVideo) {
                                    videoView?.pause()
                                } else {
                                    mediaPlayer?.pause()
                                }
                            } else {
                                if (isVideo) {
                                    videoView?.start()
                                } else {
                                    mediaPlayer?.start()
                                }

                                // Notify connected speakers that media is playing
                                if (connectedSpeakers.isNotEmpty()) {
                                    notifySpeakersMediaPlaying(connectedSpeakers)
                                }
                            }
                            isPlaying = !isPlaying
                        }) {
                            Text(if (isPlaying) "Pause" else "Play")
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Button(onClick = {
                            if (isVideo) {
                                videoView?.stopPlayback()
                                videoView?.setVideoURI(uri)
                            } else {
                                mediaPlayer?.stop()
                                mediaPlayer?.prepare()
                            }
                            isPlaying = false

                            // Notify connected speakers that media stopped
                            if (connectedSpeakers.isNotEmpty()) {
                                notifySpeakersMediaStopped(connectedSpeakers)
                            }
                        }) {
                            Text("Stop")
                        }
                    }
                }
            }
        }
    }
}

fun requestMediaPermissionAndLaunch(
    context: Context,
    permissionLauncher: androidx.activity.result.ActivityResultLauncher<Array<String>>,
    fileLauncher: androidx.activity.result.ActivityResultLauncher<String>,
    mimeType: String
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED
        ) {
            fileLauncher.launch(mimeType)
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
            fileLauncher.launch(mimeType)
        } else {
            permissionLauncher.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
        }
    }
}

fun listenForSpeakerResponses(onSpeakerFound: (String) -> Unit) {
    CoroutineScope(Dispatchers.IO).launch {
        var socket: DatagramSocket? = null
        try {
            socket = DatagramSocket(9877)
            socket.soTimeout = 5000  // 5 second timeout for receive

            val buffer = ByteArray(256)

            while (true) {
                try {
                    val packet = DatagramPacket(buffer, buffer.size)
                    socket.receive(packet)

                    val message = String(packet.data, 0, packet.length)
                    val speakerIp = packet.address.hostAddress ?: "Unknown"

                    Log.d("SpeakerListener", "Received Response: $message from $speakerIp")

                    if (message.startsWith("SPEAKER_RESPONSE:")) {
                        val reportedIp = message.removePrefix("SPEAKER_RESPONSE:")
                        Log.d("SpeakerListener", "Speaker Found: $reportedIp (actual: $speakerIp)")

                        // Use both the reported IP and actual IP for better identification
                        Handler(Looper.getMainLooper()).post {
                            onSpeakerFound(speakerIp)
                            if (reportedIp != speakerIp && reportedIp != "Unknown") {
                                onSpeakerFound("$reportedIp (reported)")
                            }
                        }
                    }
                } catch (e: java.net.SocketTimeoutException) {
                    // This is expected, just continue
                    continue
                }
            }
        } catch (e: Exception) {
            Log.e("SpeakerListener", "Error listening for speakers", e)
        } finally {
            socket?.close()
        }
    }
}

fun notifySpeakersMediaPlaying(speakers: List<String>) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val socket = DatagramSocket()
            val message = "MEDIA_PLAYING".toByteArray()

            for (speaker in speakers) {
                try {
                    val speakerAddress = InetAddress.getByName(speaker.split(" ")[0]) // Remove "(reported)" if present
                    val packet = DatagramPacket(message, message.size, speakerAddress, 9878)
                    socket.send(packet)
                    Log.d("MediaControl", "Sent play notification to $speaker")
                } catch (e: Exception) {
                    Log.e("MediaControl", "Error sending play notification to $speaker", e)
                }
            }
            socket.close()
        } catch (e: Exception) {
            Log.e("MediaControl", "Error notifying speakers", e)
        }
    }
}

fun notifySpeakersMediaStopped(speakers: List<String>) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val socket = DatagramSocket()
            val message = "MEDIA_STOPPED".toByteArray()

            for (speaker in speakers) {
                try {
                    val speakerAddress = InetAddress.getByName(speaker.split(" ")[0]) // Remove "(reported)" if present
                    val packet = DatagramPacket(message, message.size, speakerAddress, 9878)
                    socket.send(packet)
                    Log.d("MediaControl", "Sent stop notification to $speaker")
                } catch (e: Exception) {
                    Log.e("MediaControl", "Error sending stop notification to $speaker", e)
                }
            }
            socket.close()
        } catch (e: Exception) {
            Log.e("MediaControl", "Error notifying speakers", e)
        }
    }
}