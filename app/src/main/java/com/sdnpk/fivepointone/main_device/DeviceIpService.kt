package com.sdnpk.fivepointone.main_device

import android.content.Context
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import java.net.InetAddress

//class DeviceIpService(private val context: Context) {
//    fun getDeviceIpAddress(): String {
//        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
//        val wifiInfo: WifiInfo = wifiManager.connectionInfo
//        val ipAddress = wifiInfo.ipAddress
//        return formatIpAddress(ipAddress)
//    }
//
//    private fun formatIpAddress(ip: Int): String {
//        return (ip and 0xFF).toString() + "." +
//                (ip shr 8 and 0xFF).toString() + "." +
//                (ip shr 16 and 0xFF).toString() + "." +
//                (ip shr 24 and 0xFF).toString()
//    }
//}
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
