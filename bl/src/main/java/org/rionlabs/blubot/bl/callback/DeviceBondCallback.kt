package org.rionlabs.blubot.bl.callback

import org.rionlabs.blubot.bl.Device

interface DeviceBondCallback {

    fun onBondStarted(device: Device)

    fun onBonded(device: Device)

    fun onBondEnded(device: Device)
}