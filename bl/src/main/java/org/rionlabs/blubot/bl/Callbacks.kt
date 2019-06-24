package org.rionlabs.blubot.bl

import android.bluetooth.BluetoothDevice

interface BluetoothStateCallback {

    fun onBluetoothStateChanged(current: BluetoothState, previous: BluetoothState)
}

interface DiscoveryStateCallback {

    fun onDiscoveryStateChanged(isDiscovering: Boolean)
}

interface DeviceDiscoveryCallback {

    fun onDeviceDiscovered(bluetoothDevice: BluetoothDevice)
}

interface DeviceBondCallback {

    fun onConnectionStarted(bluetoothDevice: BluetoothDevice)

    fun onConnected(bluetoothDevice: BluetoothDevice)

    fun onConnectionEnded(bluetoothDevice: BluetoothDevice)
}