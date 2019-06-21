package org.rionlabs.blubot.util

import android.bluetooth.BluetoothDevice
import org.rionlabs.blubot.DiscoveredDevice

fun BluetoothDevice.dataItem(): DiscoveredDevice {
    return DiscoveredDevice(name.orEmpty(), address.orEmpty())
}