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
import android.net.wifi.WifiNetworkSuggestion
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.wifimanager.data.WifiData
import com.example.wifimanager.ui.WifiConnectScreen
import com.example.wifimanager.ui.WifiDetailScreen
import com.example.wifimanager.ui.WifiListScreen
import com.example.wifimanager.ui.WifiViewModel
import com.example.wifimanager.ui.theme.WiFiManagerTheme
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

enum class ScreenType{
    WifiList,
    WifiDetail,
    WifiConnect,
}

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.R)
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
                WifiApp(
                    wifiScanFunc = { asyncScanWifiNetworks(context = applicationContext) },
                    wifiConnectFunc = { wifiData, password ->
                        val result = addSuggestionWifi(context = applicationContext, wifiData, password)
                        if (result) {
                            AlertDialog.Builder(this)
                                .setTitle("接続試行中")
                                .setMessage("Wi-Fiネットワークへの切り替えを試みました。接続状況を確認するか、設定画面で手動でWi-Fiを選択してください。")
                                .setPositiveButton("Wi-Fi設定") { dialog, which ->
                                    // Wi-Fi設定画面へのインテント
                                    val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
                                    startActivity(intent)
                                }
                                .setNegativeButton("閉じる", null)
                                .show()
                        }else{
                            AlertDialog.Builder(this)
                                .setTitle("接続エラー")
                                .setMessage("Wi-Fi接続に失敗しました。詳細設定を確認してください。")
                                .setPositiveButton("設定") { dialog, which ->
                                    // アプリの設定画面へのインテント
                                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                        data = Uri.fromParts("package", packageName, null)
                                    }
                                    startActivity(intent)
                                }
                                .setNegativeButton("キャンセル", null)
                                .show()
                        }
                    }
                )
            }
        }
    }

    private fun showPermissionExplanationDialog() {
        AlertDialog.Builder(this)
            .setTitle("パーミッションが必要です")
            .setMessage("この機能を使用するには位置情報のパーミッションが必要です。アプリの設定画面からパーミッションを有効にしてください。")
            .setPositiveButton("設定へ移動") { _, _ ->
                // ユーザーが「設定へ移動」をタップした場合、アプリの設定画面を開く
                val intent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:$packageName")
                )
                startActivity(intent)
            }
            .setNegativeButton("キャンセル") { dialog, _ ->
                // ユーザーが「キャンセル」をタップした場合、ダイアログを閉じる
                dialog.dismiss()
            }
            .show()
    }

    private suspend fun asyncScanWifiNetworks(context: Context): List<ScanResult> =
        suspendCoroutine { cont ->
            val wifiManager =
                context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
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

    @RequiresApi(Build.VERSION_CODES.R)
    private fun addSuggestionWifi(
        context: Context,
        connectWifiData: WifiData,
        networkPass: String
    ): Boolean{

        //  サジェスチョンの生成
        //  TODO : セキュリティがWpa2を強制的に選択しているので、選べるようにする
        val suggestion = WifiNetworkSuggestion.Builder()
            .setSsid(connectWifiData.SSID)
            .setWpa2Passphrase(networkPass)
            .setIsAppInteractionRequired(true) // Optional (Needs location permission)
            .build();

        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager;

        val status = wifiManager.addNetworkSuggestions(listOf(suggestion));

        //  失敗したら
        if (status != WifiManager.STATUS_NETWORK_SUGGESTIONS_SUCCESS) {
            return false
        }

        // Optional (Wait for post connection broadcast to one of your suggestions)
        val intentFilter = IntentFilter(WifiManager.ACTION_WIFI_NETWORK_SUGGESTION_POST_CONNECTION);

        val broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (!intent.action.equals(WifiManager.ACTION_WIFI_NETWORK_SUGGESTION_POST_CONNECTION)) {
                    return;
                }
            }
        };
        context.registerReceiver(broadcastReceiver, intentFilter);

        return true
    }
}
@Composable
fun WifiApp(
    modifier: Modifier = Modifier,
    wifiScanFunc: suspend ()->List<ScanResult>,
    wifiConnectFunc: (WifiData,String)-> Unit,
    wifiViewModel: WifiViewModel = viewModel(),
    navController: NavHostController = rememberNavController()
){
    Scaffold { innerPadding->

        val wifiState by wifiViewModel.uiState.collectAsState()
        val animSpec: FiniteAnimationSpec<IntOffset> = tween(500, easing = FastOutSlowInEasing)

        val scope = rememberCoroutineScope()

        NavHost(
            navController = navController,
            startDestination = ScreenType.WifiList.name,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { screenWidth -> screenWidth },
                    animationSpec = animSpec
                )
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { screenWidth -> -screenWidth },
                    animationSpec = animSpec
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { screenWidth -> -screenWidth },
                    animationSpec = animSpec
                )
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { screenWidth -> screenWidth },
                    animationSpec = animSpec
                )
            },
            modifier = Modifier.padding(innerPadding)
        ){

            composable(route = ScreenType.WifiList.name){
                WifiListScreen (
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
                    onClickCard = {
                        wifiViewModel.selectedWifiData(it)
                        navController.navigate(route=ScreenType.WifiConnect.name)
                    },
                    onLongClickCard = {
                        wifiViewModel.selectedWifiData(it)
                        navController.navigate(route=ScreenType.WifiDetail.name)
                    }
                )
            }

            composable(route = ScreenType.WifiDetail.name){
                WifiDetailScreen(
                    wifiData = wifiState.selectedWifiData,
                    onBackClicked = {
                        navController.popBackStack()
                    }
                )
            }

            composable(route = ScreenType.WifiConnect.name){
                WifiConnectScreen(
                    wifiData = wifiState.selectedWifiData,
                    password = wifiState.password,
                    onClickDetail = {
                        wifiViewModel.selectedWifiData(it)
                        navController.navigate(route=ScreenType.WifiDetail.name)
                    },
                    onClickCancel = {
                        wifiViewModel.setWifiPassword("")
                        navController.popBackStack()
                    },
                    onClickConnect = {
                        val result = wifiConnectFunc(wifiState.selectedWifiData, wifiState.password)
                    },
                    onPasswordChanged = {
                        wifiViewModel.setWifiPassword(it)
                    },
                    onPasswordClear = {
                        wifiViewModel.setWifiPassword("")
                    }
                )
            }
        }



    }
}


@Composable
@Preview(showBackground = true)
fun PreviewWifiApp(){
    WifiApp(
        wifiScanFunc = { listOf()},
        wifiConnectFunc = {_,_-> false}
    )
}