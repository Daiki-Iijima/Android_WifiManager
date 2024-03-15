package com.example.wifimanager.ui

import android.net.wifi.ScanResult
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wifimanager.data.WifiData
import com.example.wifimanager.data.WifiUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WifiViewModel:ViewModel() {
    private val _uiState = MutableStateFlow(WifiUiState())
    val uiState: StateFlow<WifiUiState> = _uiState.asStateFlow()

    //  suspendがついている関数は非同期で呼び出すことを強制できる
    fun updateWifiList(wifiScan: suspend ()->List<ScanResult>){
        //  ロード中フラグをON
        _uiState.value = _uiState.value.copy(isLoading = true)

        //  コルーチン
        viewModelScope.launch {
            //  スキャンして結果を取得
            val scanResults = wifiScan()

            //  ScanResultクラスをWifiDataクラスに変換
            val newWifiList = scanResults.map { result ->
                WifiData(
                    BSSID = result.BSSID,
                    SSID = result.SSID,
                    capabilities = result.capabilities,
                    frequency = result.frequency,
                    level = result.level,
                    channelWidth = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) result.channelWidth else null,
                    centerFreq0 = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) result.centerFreq0 else null,
                    centerFreq1 = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) result.centerFreq1 else null,
                    timestamp = result.timestamp
                )
            }

            //  ロード中フラグをOFF
            //  Wifiリストを設定
            _uiState.value = _uiState.value.copy(
                wifiList = newWifiList,
                isLoading = false,
            )
        }
    }
}