package org.rionlabs.blubot.bl

import android.bluetooth.BluetoothDevice

fun BluetoothDevice.dataItem(connected: Boolean = false): Device {
    return Device(name.orEmpty(), address.orEmpty(), this, connected)
}

fun Int.bondStateString(): String {
    return when (this) {
        10 -> "BOND_NONE"
        11 -> "BOND_BONDING"
        12 -> "BOND_BONDED"
        else -> "BOND_UNKNOWN"
    }
}