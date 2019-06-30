package org.rionlabs.blubot.bl

interface BluetoothStateCallback {

    fun onBluetoothStateChanged(current: BluetoothState, previous: BluetoothState)
}

interface DiscoveryStateCallback {

    fun onDiscoveryStateChanged(isDiscovering: Boolean)
}

interface DeviceDiscoveryCallback {

    fun onDeviceDiscovered(device: Device)
}

interface DeviceBondCallback {

    fun onConnectionStarted(device: Device)

    fun onConnected(device: Device)

    fun onConnectionEnded(device: Device)
}