package org.rionlabs.blubot.bl.callback

import org.rionlabs.blubot.bl.BluetoothState

interface BluetoothStateCallback {

    fun onBluetoothStateChanged(current: BluetoothState, previous: BluetoothState)
}