package com.example.wifimanager.data

import android.net.wifi.ScanResult

data class WifiUiState(
    val wifiList: List<WifiData> = emptyList(),
    val filterValue: String = "",
    val connectedWifiSsid: String? = null,
    val isLoading: Boolean = false,
)
