package org.rionlabs.blubot.service

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