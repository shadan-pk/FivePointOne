package com.sdnpk.fivepointone.main_device

// For Coroutines
import com.sdnpk.fivepointone.config.Role
import com.sdnpk.fivepointone.data.SpeakerDevice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// For Networking
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

// For JSON (org.json library)
import org.json.JSONObject

fun assignRoleToSpeaker(speaker: SpeakerDevice, role: Role) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val socket = DatagramSocket()
            val address = InetAddress.getByName(speaker.ip)

            val roleData = JSONObject().apply {
                put("type", "role_assignment")
                put("assigned_role", role.name)
                put("sample_rate", 48000)
                put("bit_depth", 16)
                put("stream_protocol", "UDP_UNICAST")
            }.toString().toByteArray()

            val packet = DatagramPacket(roleData, roleData.size, address, 5005)
            socket.send(packet)

            withContext(Dispatchers.Main) {
                speaker.assignedRole = role
            }

        } catch (e: Exception) {
            println("Failed to assign role to ${speaker.id}: ${e.message}")
        }
    }
}
