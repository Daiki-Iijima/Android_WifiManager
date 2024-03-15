package com.example.wifimanager.data

data class WifiUiState(
    val wifiList: List<String> = emptyList(),
    val connectedWifiSsid: String? = null,
    val isLoading: Boolean = false,
)
