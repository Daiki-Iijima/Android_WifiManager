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
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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
    navController: NavHostController = rememberNavController()
){
    Scaffold(
    ) {innerPadding->

        val wifiState by wifiViewModel.uiState.collectAsState()
        val animSpec: FiniteAnimationSpec<IntOffset> = tween(500, easing = FastOutSlowInEasing)

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
        }



    }
}


@Composable
@Preview(showBackground = true)
fun PreviewWifiApp(){
    WifiApp(wifiScanFunc = { listOf()})
}

