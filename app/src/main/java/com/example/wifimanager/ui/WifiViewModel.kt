package com.example.wifimanager.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wifimanager.data.WifiUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WifiViewModel:ViewModel() {
    private val _uiState = MutableStateFlow(WifiUiState())
    val uiState: StateFlow<WifiUiState> = _uiState.asStateFlow()

    //  suspendがついている関数は非同期で呼び出すことを強制できる
    fun updateWifiList(wifiScan: suspend ()->List<String>){
        _uiState.value = _uiState.value.copy(isLoading = true)

        //  コルーチン
        viewModelScope.launch {
            val newWifiList = wifiScan()

            _uiState.value = _uiState.value.copy(
                wifiList = newWifiList,
                isLoading = false,
            )
        }
    }
}