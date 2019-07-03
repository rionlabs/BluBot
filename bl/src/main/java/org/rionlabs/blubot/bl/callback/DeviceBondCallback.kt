package org.rionlabs.blubot.bl.callback

import org.rionlabs.blubot.bl.Device

interface DeviceBondCallback {

    fun onConnectionStarted(device: Device)

    fun onConnected(device: Device)

    fun onConnectionEnded(device: Device)
}