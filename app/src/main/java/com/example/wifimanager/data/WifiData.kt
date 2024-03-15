package com.example.wifimanager.data

data class WifiData(
    val BSSID: String = "",
    val SSID: String = "",
    val capabilities: String = "",
    val frequency: Int = 0,
    val level: Int = 0,
    val channelWidth: Int? = null, // Android 6.0 (APIレベル 23)以降でのみ利用可能なため、null許容型としています
    val centerFreq0: Int? = null, // 同上
    val centerFreq1: Int? = null, // 同上
    val timestamp: Long = 0
)
