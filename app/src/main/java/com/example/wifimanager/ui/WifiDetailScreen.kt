package com.example.wifimanager.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.wifimanager.data.WifiData

@Composable
fun WifiDetailScreen(
    modifier: Modifier = Modifier,
    wifiData: WifiData,
    onBackClicked: ()->Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.Start,
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "WiFi 詳細", style = MaterialTheme.typography.headlineMedium)
        Divider()
        Text(text = "BSSID: ${wifiData.BSSID}")
        Text(text = "SSID: ${wifiData.SSID}")
        Text(text = "セキュリティ: ${wifiData.capabilities}")
        Text(text = "周波数: ${wifiData.frequency} MHz")
        Text(text = "信号レベル: ${wifiData.level} dBm")
        wifiData.channelWidth?.let { Text(text = "チャネル幅: $it") }
        wifiData.centerFreq0?.let { Text(text = "センター周波数 0: $it MHz") }
        wifiData.centerFreq1?.let { Text(text = "センター周波数 1: $it MHz") }
        Text(text = "タイムスタンプ: ${wifiData.timestamp}")
        // 余ったスペースを占めるためにweightを使用
        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = onBackClicked,
            modifier=Modifier.fillMaxWidth().heightIn(50.dp)
            ) {
            Text(text = "閉じる")
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun WifiDetailPreview(){
    // テスト用のデータを作成
    val testWifiData = WifiData(
        BSSID = "00:11:22:33:44:55",
        SSID = "TestNetwork",
        capabilities = "[WPA2-PSK-CCMP][ESS]",
        frequency = 2462,
        level = -45,
        channelWidth = 40,
        centerFreq0 = 2452,
        centerFreq1 = 2472,
        timestamp = System.currentTimeMillis()
    )

    WifiDetailScreen(wifiData = testWifiData, onBackClicked = {})
}