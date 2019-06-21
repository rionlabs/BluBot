package org.rionlabs.blubot

import android.bluetooth.BluetoothDevice

data class DiscoveredDevice(
    val name: String,
    val ssid: String,
    val bluetoothDevice: BluetoothDevice
)