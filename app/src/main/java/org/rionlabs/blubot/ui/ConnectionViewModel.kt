package org.rionlabs.blubot.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.rionlabs.blubot.bl.Device
import org.rionlabs.blubot.bl.callback.DeviceBondCallback
import org.rionlabs.blubot.bl.callback.DeviceConnectionCallback
import org.rionlabs.blubot.bl.callback.DeviceDiscoveryCallback
import org.rionlabs.blubot.bl.callback.DiscoveryStateCallback
import org.rionlabs.blubot.service.bluetoothManager

class ConnectionViewModel(app: Application) : AndroidViewModel(app), DeviceDiscoveryCallback,
    DiscoveryStateCallback, DeviceBondCallback, DeviceConnectionCallback {

    private val _deviceList = MutableLiveData<List<Device>>()
    val deviceListData: LiveData<List<Device>>
        get() = _deviceList

    init {
        // Link BL Manager with system
        bluetoothManager.registerListeners()
        // Start listening to callbacks
        bluetoothManager.addDeviceDiscoveryCallback(this)
        bluetoothManager.addDiscoveryStateCallback(this)
        bluetoothManager.addDeviceBondCallback(this)
        bluetoothManager.addDeviceConnectionCallback(this)
    }

    // region BluetoothManager Callbacks

    override fun onDiscoveryStateChanged(isDiscovering: Boolean) {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onDeviceDiscovered(device: Device) {
        val deviceList = _deviceList.value?.toMutableList() ?: mutableListOf()
        deviceList.add(device)
        _deviceList.postValue(deviceList)
    }

    override fun onBondStarted(device: Device) {
        val deviceList = _deviceList.value?.toMutableList() ?: mutableListOf()
        replaceDevice(deviceList, device)
        _deviceList.postValue(deviceList)
    }

    override fun onBonded(device: Device) {
        val deviceList = _deviceList.value?.toMutableList() ?: mutableListOf()
        replaceDevice(deviceList, device)
        _deviceList.postValue(deviceList)
    }

    override fun onBondEnded(device: Device) {
        val deviceList = _deviceList.value?.toMutableList() ?: mutableListOf()
        replaceDevice(deviceList, device)
        _deviceList.postValue(deviceList)
    }

    override fun onConnectionStateChanged(device: Device) {
        val deviceList = _deviceList.value?.toMutableList() ?: mutableListOf()
        replaceDevice(deviceList, device)
        _deviceList.postValue(deviceList)
    }

    // endregion

    private fun replaceDevice(deviceList: MutableList<Device>, newDevice: Device) {
        val oldDevice = deviceList.find { it.ssid == newDevice.ssid }
        oldDevice?.let { deviceList.remove(it) }
        deviceList.add(newDevice)
    }

    override fun onCleared() {
        // Clear callbacks
        bluetoothManager.removeDiscoveryStateCallback(this)
        bluetoothManager.removeDeviceDiscoveryCallback(this)
        bluetoothManager.removeDeviceBondCallback(this)
        bluetoothManager.removeDeviceConnectionCallback(this)
        // Clean up BL Manager
        bluetoothManager.unregisterReceivers()
    }
}