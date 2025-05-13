package com.sdnpk.fivepointone

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import android.widget.VideoView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.NavController

@Composable
fun MediaPlaybackScreen(navController: NavController) {
    val context = LocalContext.current
    val mediaUri = remember { mutableStateOf<Uri?>(null) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var videoView by remember { mutableStateOf<VideoView?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var fileName by remember { mutableStateOf("") }
    var isVideo by remember { mutableStateOf(false) }

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

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            val allGranted = permissions.all { it.value }
            hasPermission = allGranted
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

    // Clean up MediaPlayer and VideoView when leaving the composition
    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
            mediaPlayer = null
            videoView?.stopPlayback()
            videoView = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Media Player",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(32.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED
                    ) {
                        openFileLauncher.launch("audio/*")
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
                        openFileLauncher.launch("audio/*")
                    } else {
                        permissionLauncher.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
                    }
                }
            }) {
                Text("Load Audio")
            }
            Button(onClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED
                    ) {
                        openFileLauncher.launch("video/*")
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
                        openFileLauncher.launch("video/*")
                    } else {
                        permissionLauncher.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
                    }
                }
            }) {
                Text("Load Video")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        mediaUri.value?.let { uri ->
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
                        text = "Now Playing: $fileName",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    if (isVideo) {
                        AndroidView(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp),
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
                        }) {
                            Text("Stop")
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            navController.popBackStack()
        }) {
            Text("Back to Main Screen")
        }
    }
}