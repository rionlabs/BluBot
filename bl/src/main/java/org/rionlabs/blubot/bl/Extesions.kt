package org.rionlabs.blubot.bl

import android.bluetooth.BluetoothDevice

fun BluetoothDevice.dataItem(): Device {
    return Device(name.orEmpty(), address.orEmpty(), this, ConnectionState.NONE)
}

fun Int.bondStateString(): String {
    return when (this) {
        10 -> "BOND_NONE"
        11 -> "BOND_BONDING"
        12 -> "BOND_BONDED"
        else -> "BOND_UNKNOWN"
    }
}