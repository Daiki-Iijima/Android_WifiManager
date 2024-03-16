package com.example.wifimanager

import android.Manifest
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wifimanager.ui.WifiListScreen
import com.example.wifimanager.ui.WifiViewModel
import com.example.wifimanager.ui.theme.WiFiManagerTheme
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
                WifiApp(wifiScanFunc = { asyncScanWifiNetworks(context = applicationContext) })
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

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            // ユーザーがパーミッションリクエストを一度拒否した場合、説明用のダイアログを表示
            showPermissionExplanationDialog()

            cont.resume(listOf())
        } else {
            // パーミッションリクエストをまだ行っていない、または「今後表示しない」をユーザーが選択していない場合
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        }


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
}

@Composable
fun WifiApp(
    modifier: Modifier = Modifier,
    wifiScanFunc: suspend ()->List<ScanResult>,
    wifiViewModel: WifiViewModel = viewModel(),
){
    Scaffold(
    ) {innerPadding->
        val wifiState by wifiViewModel.uiState.collectAsState()

        WifiListScreen (
            modifier = Modifier.padding(innerPadding),
            wifiList = wifiState.wifiList,
            filterValue = wifiState.filterValue,
            isLoading = wifiState.isLoading,
            onWifiScan = {
                    wifiViewModel.updateWifiList(
                        wifiScan = wifiScanFunc
                    )
                },
            onFilterValueChanged = {
                wifiViewModel.updateFilterValue(it)
            },
            onFilterClear = {
                wifiViewModel.updateFilterValue("")
            },
        )
    }
}


@Composable
@Preview(showBackground = true)
fun PreviewWifiApp(){
    WifiApp(wifiScanFunc = { listOf()})
}

