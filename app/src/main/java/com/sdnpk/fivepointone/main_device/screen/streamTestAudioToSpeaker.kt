package com.sdnpk.fivepointone.main_device.screen

import android.content.Context
import com.sdnpk.fivepointone.R
import com.sdnpk.fivepointone.data.SpeakerDevice
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress


fun streamTestAudioToSpeaker(context: Context, speaker: SpeakerDevice) {
    Thread {
        try {
            val inputStream = context.resources.openRawResource(R.raw.test_sound)
            val socket = DatagramSocket()
            val buffer = ByteArray(1024)
            val address = InetAddress.getByName(speaker.ip)

            var len: Int
            while (inputStream.read(buffer).also { len = it } != -1) {
                val packet = DatagramPacket(buffer, len, address, 50005)
                socket.send(packet)
                Thread.sleep(10) // minimal delay between packets
            }

            socket.close()
            inputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }.start()
}
