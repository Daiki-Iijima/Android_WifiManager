package com.example.wifimanager.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.wifimanager.data.WifiData

@Composable
fun WifiConnectScreen(
    modifier: Modifier = Modifier,
    wifiData: WifiData,
    password: String,
    onClickDetail: (WifiData)->Unit,
    onClickCancel: ()->Unit,
    onClickConnect: ()->Unit,
    onPasswordChanged: (String)->Unit,
    onPasswordClear: ()->Unit,
){

    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Row (
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
        ){
            Text(
                text = "SSID",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = {onClickDetail(wifiData)}) {
                Icon(imageVector = Icons.Filled.Info, contentDescription = "")
            }
        }
        Text(
            text = wifiData.SSID,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = password,
            label = {
                Text(text = "パスワード")
            },
            maxLines = 1,
            onValueChange = onPasswordChanged,
            trailingIcon = {
                if(password.isNotEmpty()) {
                    IconButton(
                        onClick = onPasswordClear
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                            modifier = Modifier
                        )
                    }
                }
            },
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()   //  フォーカスを外す
                }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )
        Row {
            Button(
                onClick = onClickCancel,
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "キャンセル")
            }
            Button(
                onClick = onClickConnect,
                modifier = Modifier.weight(1f),
                enabled = password.isNotEmpty()
            ) {
                Text(text = "接続")
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun WifiConnectPreview(){
    // テスト用のデータを作成
    val testWifiData = WifiData(
        BSSID = "00:11:22:33:44:55",
        SSID = "TestNetwork",
        capabilities = "[WPA2-PSK-CAMP][ESS]",
        frequency = 2462,
        level = -45,
        channelWidth = 40,
        centerFreq0 = 2452,
        centerFreq1 = 2472,
        timestamp = System.currentTimeMillis()
    )

    WifiConnectScreen(
        wifiData = testWifiData,
        password = "",
        onClickDetail = {},
        onClickCancel = {},
        onClickConnect = {},
        onPasswordChanged = {},
        onPasswordClear = {},
    )
}
