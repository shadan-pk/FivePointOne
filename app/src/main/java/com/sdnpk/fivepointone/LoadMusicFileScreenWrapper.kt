package com.sdnpk.fivepointone

import androidx.compose.runtime.*

@Composable
fun LoadMusicFileScreenWrapper() {
    var hasPermission by remember { mutableStateOf(false) }
    var checkedPermission by remember { mutableStateOf(false) }

    RequestStoragePermission(
        onPermissionGranted = {
            hasPermission = true
            checkedPermission = true
        },
        onPermissionDenied = {
            hasPermission = false
            checkedPermission = true
        }
    )

    if (checkedPermission) {
        if (hasPermission) {
            LoadMusicFileScreen()
        } else {
            PermissionDeniedScreen()
        }
    }
}

@Composable
fun PermissionDeniedScreen() {
    // Simple fallback screen if permission is denied
    androidx.compose.material3.Text("Permission denied. Please enable storage permission in settings.")
}
