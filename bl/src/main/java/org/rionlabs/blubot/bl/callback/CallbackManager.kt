package org.rionlabs.blubot.bl.callback

abstract class CallbackManager {

    protected val bluetoothStateCallbackList = mutableListOf<BluetoothStateCallback>()

    protected val discoveryStateCallbackList = mutableListOf<DiscoveryStateCallback>()

    protected val deviceDiscoveryCallbackList = mutableListOf<DeviceDiscoveryCallback>()

    protected val deviceBondCallbackList = mutableListOf<DeviceBondCallback>()

    fun addBluetoothStateCallback(stateCallback: BluetoothStateCallback) {
        bluetoothStateCallbackList.add(stateCallback)
    }

    fun removeBluetoothStateCallback(stateCallback: BluetoothStateCallback) {
        bluetoothStateCallbackList.remove(stateCallback)
    }

    fun addDiscoveryStateCallback(stateCallback: DiscoveryStateCallback) {
        discoveryStateCallbackList.add(stateCallback)
    }

    fun removeDiscoveryStateCallback(stateCallback: DiscoveryStateCallback) {
        discoveryStateCallbackList.remove(stateCallback)
    }

    fun addDeviceDiscoveryCallback(deviceCallback: DeviceDiscoveryCallback) {
        deviceDiscoveryCallbackList.add(deviceCallback)
    }

    fun removeDeviceDiscoveryCallback(deviceCallback: DeviceDiscoveryCallback) {
        deviceDiscoveryCallbackList.remove(deviceCallback)
    }

    fun addDeviceBondCallback(deviceBondCallback: DeviceBondCallback) {
        deviceBondCallbackList.add(deviceBondCallback)
    }

    fun removeDeviceBondCallback(deviceBondCallback: DeviceBondCallback) {
        deviceBondCallbackList.remove(deviceBondCallback)
    }
}