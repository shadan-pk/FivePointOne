package com.sdnpk.fivepointone.main_device

import android.content.Context
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import java.net.InetAddress

fun getDeviceIpAddress(context: Context): String {
    val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    val ip = wm.connectionInfo.ipAddress
    return InetAddress.getByAddress(
        byteArrayOf(
            (ip and 0xff).toByte(),
            (ip shr 8 and 0xff).toByte(),
            (ip shr 16 and 0xff).toByte(),
            (ip shr 24 and 0xff).toByte()
        )
    ).hostAddress ?: "Unknown"
}
