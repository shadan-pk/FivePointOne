package com.sdnpk.fivepointone.speaker_device.audio

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import java.net.DatagramPacket
import java.net.DatagramSocket

fun startAudioStreamingReceiver(port: Int = 50005) {
    // AudioTrack parameters
    val sampleRate = 44100
    val channelConfig = AudioFormat.CHANNEL_OUT_MONO
    val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    val minBufferSize = AudioTrack.getMinBufferSize(sampleRate, channelConfig, audioFormat)

    val audioTrack = AudioTrack(
        AudioManager.STREAM_MUSIC,
        sampleRate,
        channelConfig,
        audioFormat,
        minBufferSize,
        AudioTrack.MODE_STREAM
    )
    audioTrack.play()

    // UDP socket to receive audio packets
    val socket = DatagramSocket(port)
    val buffer = ByteArray(minBufferSize)
    var isReceiving = true

    Thread {
        try {
            while (isReceiving) {
                val packet = DatagramPacket(buffer, buffer.size)
                socket.receive(packet)
                audioTrack.write(packet.data, 0, packet.length)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            audioTrack.stop()
            audioTrack.release()
            socket.close()
        }
    }.start()
}
