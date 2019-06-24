package org.rionlabs.blubot.util

import android.bluetooth.BluetoothDevice
import org.rionlabs.blubot.bl.Device

fun BluetoothDevice.dataItem(): Device {
    return Device(name.orEmpty(), address.orEmpty(), this)
}