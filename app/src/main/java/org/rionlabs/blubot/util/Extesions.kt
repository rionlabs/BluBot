package org.rionlabs.blubot.util

import android.bluetooth.BluetoothDevice
import org.rionlabs.blubot.bl.DiscoveredDevice

fun BluetoothDevice.dataItem(): DiscoveredDevice {
    return DiscoveredDevice(name.orEmpty(), address.orEmpty(), this)
}