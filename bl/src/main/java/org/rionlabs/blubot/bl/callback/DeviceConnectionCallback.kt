package org.rionlabs.blubot.bl.callback

import org.rionlabs.blubot.bl.Device

interface DeviceConnectionCallback {

    fun onConnectionStateChanged(device: Device)
}