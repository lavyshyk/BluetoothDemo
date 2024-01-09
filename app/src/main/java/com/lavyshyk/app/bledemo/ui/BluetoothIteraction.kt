package com.lavyshyk.app.bledemo.ui

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Build
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.lavyshyk.app.bledemo.service.GATTServerService

@SuppressLint("MissingPermission")
@Composable
fun BluetoothIteractionScreen() {

    var selectedDevice by remember { mutableStateOf<BluetoothDevice?>(null) }
    val extraPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        setOf<String>(Manifest.permission.BLUETOOTH_ADVERTISE)
    } else {
        emptySet()
    }



    BluetoothBox(extraPermissions = extraPermissions) { adapter ->
        Column {
            if (adapter.isMultipleAdvertisementSupported) {
                DrivingServiceComponent()
            } else {
                Text(text = "Cannot run server:\nDevices does not support multi-advertisement")
            }

            Divider()



            AnimatedContent(targetState = selectedDevice, label = "device : ${selectedDevice?.name ?: ""}") { device ->
                if (device == null) {
                    FindDevicesScreen() {
                        selectedDevice = it
                    }
                } else {
                    ConnectedDeviceScreen(device = device) {
                        selectedDevice = null
                    }
                }
            }
        }
    }
}

@Composable
fun DrivingServiceComponent() {

    val context = LocalContext.current
    var enableServer by remember {
        mutableStateOf(GATTServerService.isServerRunning.value)
    }
    var enableAdvertising by remember(enableServer) {
        mutableStateOf(enableServer)
    }
    val logs by GATTServerService.serverLogsState.collectAsState()

    LaunchedEffect(enableServer, enableAdvertising) {
        val intent = Intent(context, GATTServerService::class.java).apply {
            action = if (enableAdvertising) {
                GATTServerService.ACTION_START_ADVERTISING
            } else {
                GATTServerService.ACTION_STOP_ADVERTISING
            }
        }
        if (enableServer) {
            ContextCompat.startForegroundService(context, intent)
        } else {
            context.stopService(intent)
        }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        Row {
            Button(onClick = { enableServer = !enableServer }) { Text(text = (if (enableServer) "STOP SERVER" else "START SERVICE")) }
            Button(onClick = { enableServer = !enableServer }) {
                Text(
                    text = (if (enableAdvertising) "STOP ADVERTISING" else "START ADVERTISING"
                            )
                )
            }
        }
        Text(text = logs)
    }
}