package com.sdnpk.fivepointone

import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.media.MediaPlayer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.activity.result.contract.ActivityResultContracts

@Composable
fun LoadMusicFileScreen() {
    val context = LocalContext.current
    val musicUri = remember { mutableStateOf<Uri?>(null) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    val openFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            musicUri.value = uri
            // Initialize MediaPlayer with the selected music URI
            uri?.let {
                mediaPlayer = MediaPlayer.create(context, it)
            }
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = { openFileLauncher.launch("audio/*") }) {
            Text("Select Music File")
        }

        Spacer(modifier = Modifier.height(16.dp))

        musicUri.value?.let {
            Text("Music File Selected: ${it.lastPathSegment}")
            Spacer(modifier = Modifier.height(16.dp))

            Row {
                Button(onClick = {
                    mediaPlayer?.start()
                }) {
                    Text("Play")
                }

                Spacer(modifier = Modifier.width(16.dp))

                Button(onClick = {
                    mediaPlayer?.pause()
                }) {
                    Text("Pause")
                }
            }
        }
    }
}
