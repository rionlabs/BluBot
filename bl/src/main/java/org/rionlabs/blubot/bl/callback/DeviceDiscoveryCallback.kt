package org.rionlabs.blubot.bl.callback

import org.rionlabs.blubot.bl.Device

interface DeviceDiscoveryCallback {

    fun onDeviceDiscovered(device: Device)
}