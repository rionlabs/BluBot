package org.rionlabs.blubot.bl

import android.bluetooth.BluetoothDevice

fun BluetoothDevice.dataItem(): Device {
    return Device(name.orEmpty(), address.orEmpty(), this)
}