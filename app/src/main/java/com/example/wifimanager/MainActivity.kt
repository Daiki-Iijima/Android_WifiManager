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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.example.wifimanager.ui.theme.WiFiManagerTheme
import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.net.wifi.ScanResult
import android.os.Build
import android.provider.Settings
import androidx.compose.material3.IconButton
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wifimanager.data.WifiData
import com.example.wifimanager.ui.WifiViewModel
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //  位置情報権限の取得
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            1
        )

        setContent {
            WiFiManagerTheme {
                WifiListApp(scanWiFiFunc = { asyncScanWifiNetworks(context = applicationContext) })
            }
        }
    }

    private fun showPermissionExplanationDialog() {
        AlertDialog.Builder(this)
            .setTitle("パーミッションが必要です")
            .setMessage("この機能を使用するには位置情報のパーミッションが必要です。アプリの設定画面からパーミッションを有効にしてください。")
            .setPositiveButton("設定へ移動") {_,_->
                // ユーザーが「設定へ移動」をタップした場合、アプリの設定画面を開く
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName"))
                startActivity(intent)
            }
            .setNegativeButton("キャンセル") { dialog, _ ->
                // ユーザーが「キャンセル」をタップした場合、ダイアログを閉じる
                dialog.dismiss()
            }
            .show()
    }

    private suspend fun asyncScanWifiNetworks(context: Context): List<ScanResult> = suspendCoroutine { cont ->
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context, intent: Intent) {
                if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION == intent.action) {
                    val results = wifiManager.scanResults
                    context.unregisterReceiver(this) // レシーバーの登録解除
                    cont.resume(results) // スキャン結果を返す
                }
            }
        }

        // レシーバーの登録
        val filter = IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        context.registerReceiver(receiver, filter)

        // スキャンの開始
        wifiManager.startScan()
    }

    private fun scanWifiNetworks(): List<String> {
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            // ユーザーがパーミッションリクエストを一度拒否した場合、説明用のダイアログを表示
            showPermissionExplanationDialog()

            return listOf()
        } else {
            // パーミッションリクエストをまだ行っていない、または「今後表示しない」をユーザーが選択していない場合
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        }

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
    wifiList:List<WifiData>,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier,
){
    LazyColumn(modifier = modifier.fillMaxWidth()) {
        items(wifiList) { wifi ->
            WiFiInformationCard(
                ssidStr = wifi.SSID,
                onClickCard = {
                    println("選ばれし!: ${wifi.SSID}")
                },
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
fun WifiListApp(
    modifier: Modifier = Modifier,
    wifiViewModel: WifiViewModel = viewModel(),
    scanWiFiFunc: suspend ()->List<ScanResult>,
) {
    val wifiState by wifiViewModel.uiState.collectAsState()

    WiFiManagerTheme {

        Column {
            Button(
                onClick = {
                    wifiViewModel.updateWifiList(
                        wifiScan = scanWiFiFunc
                    )
                },
                enabled = !wifiState.isLoading,
                modifier = modifier.fillMaxWidth()
            ) {
                Text(text = if(wifiState.isLoading)"サーチ中" else "サーチ開始")
            }
            WiFiList(wifiState.wifiList)
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
    WiFiList(
        wifiList = listOf(
            WifiData(SSID = "test1"),
            WifiData(SSID = "test2"),
            WifiData(SSID = "test3"),
            )
    )
}
