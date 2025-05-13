package com.sdnpk.fivepointone

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.sdnpk.fivepointone.network.SpeakerNetworkManager
import com.sdnpk.fivepointone.ui.theme.FivePointOneTheme
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface

class SpeakerDeviceActivity : ComponentActivity() {
    private var speakerRole = "RearLeft" // Default role, could be configured via settings
    private var mainDeviceIp: String? = null
    private var discoverySocket: DatagramSocket? = null
    private var commandSocket: DatagramSocket? = null
    private var isListening = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Start listening for commands from the main device
        startCommandListener()

        // Broadcast availability to any main device on the network
        broadcastAvailability()

        setContent {
            FivePointOneTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SpeakerDeviceScreen(
                        navController = NavController(this),
                        speakerRole = speakerRole,
                        mainDeviceIp = mainDeviceIp
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        // Clean up resources
        isListening = false
        discoverySocket?.close()
        commandSocket?.close()
        super.onDestroy()
    }

//    private fun startCommandListener() {
//        isListening = true
//
//        Thread {
//            try {
//                commandSocket = DatagramSocket(9876)
//                val buffer = ByteArray(1024)
//
//                while (isListening) {
//                    val packet = DatagramPacket(buffer, buffer.size)
//                    commandSocket?.receive(packet)
//
//                    val message = String(packet.data, 0, packet.length)
//                    mainDeviceIp = packet.address.hostAddress
//
//                    // Process command from main device
//                    processCommand(message)
//                }
//            } catch (e: Exception) {
//                if (isListening) {
//                    e.printStackTrace()
//                }
//            }
//        }.start()
//    }

//    private fun startCommandListener() {
//        isListening = true
//
//        Thread {
//            try {
//                commandSocket = DatagramSocket(9876)
//                val buffer = ByteArray(1024)
//
//                while (isListening) {
//                    val packet = DatagramPacket(buffer, buffer.size)
//                    commandSocket?.receive(packet)
//
//                    val message = String(packet.data, 0, packet.length)
//
//                    // Only update mainDeviceIp if it's the discovery message
//                    if (message == "FIVEPOINTONE_DISCOVERY") {
//                        mainDeviceIp = packet.address.hostAddress
//                        Log.d("CommandListener", "Discovered main device at: $mainDeviceIp")
//                    } else {
//                        // Process other commands from the main device
//                        // Only process commands if we've already discovered the main device
//                        if (mainDeviceIp == packet.address.hostAddress) {
//                            processCommand(message)
//                        }
//                    }
//                }
//            } catch (e: Exception) {
//                if (isListening) {
//                    e.printStackTrace()
//                }
//            }
//        }.start()
//    }

    private fun startCommandListener() {
        isListening = true
        Log.d("CommandListener", "Starting command listener on port 9876...")

        Thread {
            try {
                Log.d("CommandListener", "Creating DatagramSocket on port 9876")
                commandSocket = DatagramSocket(9876)
                val buffer = ByteArray(1024)

                Log.d("CommandListener", "Entering listening loop")
                while (isListening) {
                    val packet = DatagramPacket(buffer, buffer.size)
                    Log.d("CommandListener", "Waiting to receive packet...")
                    commandSocket?.receive(packet)

                    val message = String(packet.data, 0, packet.length)
                    val senderIp = packet.address.hostAddress
                    Log.d("CommandListener", "Received packet from $senderIp with message: '$message'")

                    // Check if the message is from our own device
                    val isOwnIp = isOwnIpAddress(senderIp)
                    Log.d("CommandListener", "Is sender our own device? $isOwnIp")

                    // Only update mainDeviceIp if it's the discovery message and not from our own device
                    if (message.trim() == "FIVEPOINTONE_DISCOVERY") {
                        Log.d("CommandListener", "Discovery message detected")
                        if (!isOwnIp) {
                            mainDeviceIp = senderIp
                            Log.d("CommandListener", "Updated main device IP to: $mainDeviceIp")
                        } else {
                            Log.d("CommandListener", "Ignoring discovery from our own device")
                        }
                    } else {
                        Log.d("CommandListener", "Processing command message: '$message'")
                        // Process other commands from the main device
                        // Only process if from the known main device
                        if (mainDeviceIp == senderIp || mainDeviceIp == null) {
                            processCommand(message)
                        } else {
                            Log.d("CommandListener", "Ignoring command from unknown source: $senderIp")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("CommandListener", "Error in command listener", e)
                if (isListening) {
                    e.printStackTrace()
                }
            } finally {
                Log.d("CommandListener", "Command listener stopped")
            }
        }.start()
    }

    // Helper function to determine if an IP belongs to this device
    private fun isOwnIpAddress(ipAddress: String): Boolean {
        try {
            // Get all network interfaces
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()

                // Get all IP addresses assigned to this interface
                val addresses = networkInterface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val address = addresses.nextElement()

                    // Check if this address matches the given IP
                    if (!address.isLoopbackAddress &&
                        address is Inet4Address &&
                        address.hostAddress == ipAddress) {
                        return true
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("CommandListener", "Error checking own IP", e)
        }
        return false
    }

    private fun broadcastAvailability() {
        Thread {
            try {
                // Send on broadcast address to discover main device
                sendResponseToMainDevice("255.255.255.255")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    private fun sendResponseToMainDevice(mainIp: String) {
        try {
            val responseMessage = "SPEAKER_DEVICE_RESPONSE|$speakerRole"
            val responseData = responseMessage.toByteArray()
            val packet = DatagramPacket(
                responseData,
                responseData.size,
                InetAddress.getByName(mainIp),
                9877 // Main device should listen on this port
            )
            val socket = DatagramSocket()
            socket.broadcast = true // Enable broadcast
            socket.send(packet)
            println("Sent response to Main Device at $mainIp")
            socket.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun processCommand(command: String) {
        println("Received command: $command")

        when {
            command.startsWith("MEDIA_PLAY") -> {
                // Handle play command
                // Extract position if provided: MEDIA_PLAY|positionInMs
                val positionParts = command.split("|")
                val position = if (positionParts.size > 1) {
                    try {
                        positionParts[1].toInt()
                    } catch (e: NumberFormatException) {
                        0
                    }
                } else {
                    0
                }

                // TODO: Play audio with the specified position
            }

            command == "MEDIA_PAUSE" -> {
                // TODO: Pause audio playback
            }

            command == "MEDIA_STOP" -> {
                // TODO: Stop audio playback
            }

            command.startsWith("MEDIA_LOAD") -> {
                // Handle loading a specific media file
                // Format: MEDIA_LOAD|filename
                val parts = command.split("|")
                if (parts.size > 1) {
                    val filename = parts[1]
                    // TODO: Load the audio file with the given name
                }
            }
        }
    }
}

@Composable
fun SpeakerDeviceScreen(
    navController: NavController,
    speakerRole: String,
    mainDeviceIp: String?
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Speaker Device",
                    style = MaterialTheme.typography.headlineMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Role: $speakerRole",
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (mainDeviceIp != null) {
                        "Connected to: $mainDeviceIp"
                    } else {
                        "Searching for main device..."
                    },
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Audio status indicators would go here
                // This would be updated based on the current playback state

                Text(
                    text = "Audio Status: Ready",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}