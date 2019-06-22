package org.rionlabs.blubot.service

interface BluetoothStateCallback {

    fun onBluetoothStateChanged(current: BluetoothState, previous: BluetoothState)
}