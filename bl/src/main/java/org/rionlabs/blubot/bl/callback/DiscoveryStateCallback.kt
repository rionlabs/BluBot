package org.rionlabs.blubot.bl.callback

interface DiscoveryStateCallback {

    fun onDiscoveryStateChanged(isDiscovering: Boolean)
}