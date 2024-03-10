package com.example.wifimanager

import android.content.Context
import android.net.wifi.WifiManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.example.wifimanager.ui.theme.WiFiManagerTheme
import android.Manifest
import android.annotation.SuppressLint
import android.os.Build

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            1
        )

        setContent {
            WiFiManagerTheme {
                WifiListApp(scanWiFiFunc = { scanWifiNetworks() })
            }
        }
    }

    private fun scanWifiNetworks(): List<String> {
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val results = wifiManager.scanResults

        results.forEach { scanResult ->
            @Suppress("DEPRECATION")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Android 13 (API レベル 34) 以降で推奨される新しい方法を使用
                println(scanResult.wifiSsid.toString())
            } else {
                // それ以前のバージョンでの ScanResult.SSID の使用
                println(scanResult.SSID)
            }
        }

        @Suppress("DEPRECATION")
        return results.map {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Android 13 (API レベル 34) 以降で推奨される新しい方法を使用
                it.wifiSsid.toString()
            } else {
                // それ以前のバージョンでの ScanResult.SSID の使用
                it.SSID
            }
        }.distinct()
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WiFiInformationCard(
    ssidStr:String,
    onClickCard: ()->Unit,
    modifier: Modifier = Modifier
){
    Card(modifier = modifier, onClick = onClickCard) {
        Row (modifier = Modifier.fillMaxSize()){
            Text(
                text = "SSID:",
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(10.dp),
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = ssidStr,
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.headlineSmall
            )
        }
    }
}

@Composable
fun WiFiList(
    wifiList:List<String>,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier,
){
    LazyColumn(modifier = modifier.fillMaxWidth()) {
        items(wifiList) { ssidStr ->
            WiFiInformationCard(
                ssidStr = ssidStr,
                onClickCard = {
                    println("選ばれし!: $ssidStr")
                },
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
fun WifiListApp(scanWiFiFunc: ()->List<String>,modifier: Modifier = Modifier) {
    val wifiList = remember { mutableStateListOf<String>() }

    WiFiManagerTheme {

        Column {
            Button(
                onClick = {
                    wifiList.clear()
                    scanWiFiFunc().forEach { ssid ->
                        wifiList.add(ssid)
                    }
                },
                modifier = modifier.fillMaxWidth()
            ) {
                Text(text = "サーチ開始")
            }
            WiFiList(wifiList)
        }
    }
}

@Composable
@Preview(showBackground = true)
fun PreviewWifiApp(){
    WifiListApp (scanWiFiFunc = { listOf() })
}

@Composable
@Preview(showBackground = true)
fun PreviewWifiList(){
    WiFiList( wifiList = listOf("test1","test2","test3") )
}
