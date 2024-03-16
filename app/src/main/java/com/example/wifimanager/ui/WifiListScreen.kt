package com.example.wifimanager.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.wifimanager.data.WifiData
import com.example.wifimanager.ui.theme.WiFiManagerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WiFiInformationCard(
    modifier: Modifier = Modifier,
    wifiData:WifiData,
    onClickCard: ()->Unit,
){
    var expanded by rememberSaveable { mutableStateOf(false) }

    Card(modifier = modifier, onClick = onClickCard) {
        Column (
            modifier = Modifier.animateContentSize()
        ){
            Row(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = "SSID:",
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(10.dp),
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = wifiData.SSID,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    onClick = { expanded = !expanded },
                    modifier=Modifier.align(Alignment.CenterVertically)
                ) {
                    Icon(
                        imageVector = if(expanded) Icons.Filled.ExpandLess  else Icons.Filled.ExpandMore,
                        contentDescription = "詳細表示",
                    )
                }
            }
            if(expanded) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(text = "Hz : ",modifier = Modifier.padding(end = 10.dp))
                    Text(text = wifiData.frequency.toString(),modifier = Modifier.padding(end = 10.dp))
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(text = "電波強度 : ",modifier = Modifier.padding(end = 10.dp))
                    Text(text = wifiData.level.toString(),modifier = Modifier.padding(end = 10.dp))
                }
            }
        }
    }
}

@Composable
fun WiFiList(
    modifier: Modifier = Modifier,
    wifiList:List<WifiData>,
){
    LazyColumn(modifier = modifier.fillMaxWidth()) {
        items(wifiList) { wifi ->
            WiFiInformationCard(
                wifiData = wifi,
                onClickCard = {
                    println("選ばれし!: ${wifi.SSID}")
                },
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
fun WifiListScreen(
    modifier: Modifier = Modifier,
    wifiList: List<WifiData>,
    onWifiScan: ()->Unit,
    filterValue: String,
    isLoading:Boolean,
    onFilterValueChanged: (String)->Unit,
    onFilterClear:()->Unit,
) {

    val focusManager = LocalFocusManager.current

    WiFiManagerTheme {
        Column {
            OutlinedTextField(
                value = filterValue,
                maxLines = 1,
                label = {
                    Text(text = "SSIDで絞り込み")
                },
                onValueChange = onFilterValueChanged,
                trailingIcon = {
                    if(filterValue.isNotEmpty()) {
                        IconButton(
                            onClick = onFilterClear
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
                    .padding(8.dp)
            )
            Button(
                onClick = onWifiScan,
                shape = ShapeDefaults.Small,
                enabled = !isLoading,
                modifier = modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(text = if(isLoading)"サーチ中" else "サーチ開始")
            }
            WiFiList(wifiList = wifiList)
        }
    }
}

@Composable
@Preview(showBackground = true)
fun PreviewScreen(){
    WifiListScreen (
        wifiList = listOf(
            WifiData(SSID = "test1"),
            WifiData(SSID = "test2"),
            WifiData(SSID = "test3"),
        ),
        filterValue = "",
        isLoading = false,
        onWifiScan = { },
        onFilterValueChanged = { },
        onFilterClear = { },
    )
}
