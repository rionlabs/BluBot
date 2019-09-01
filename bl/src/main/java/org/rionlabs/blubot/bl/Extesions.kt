package org.rionlabs.blubot.bl

import android.bluetooth.BluetoothDevice

fun BluetoothDevice.dataItem(connected: Boolean = false): Device {
    return Device(name.orEmpty(), address.orEmpty(), this, connected)
}