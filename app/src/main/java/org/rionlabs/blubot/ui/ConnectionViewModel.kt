package org.rionlabs.blubot.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.rionlabs.blubot.bl.BluetoothState
import org.rionlabs.blubot.bl.Device
import org.rionlabs.blubot.bl.callback.*
import org.rionlabs.blubot.service.bluetoothManager

class ConnectionViewModel(app: Application) : AndroidViewModel(app), BluetoothStateCallback,
    DeviceDiscoveryCallback,
    DiscoveryStateCallback, DeviceBondCallback, DeviceConnectionCallback {

    private val _bluetoothState = MutableLiveData<BluetoothState>()
    val bluetoothStateData: LiveData<BluetoothState>
        get() = _bluetoothState

    private val _deviceList = MutableLiveData<List<Device>>()
    val deviceListData: LiveData<List<Device>>
        get() = _deviceList

    private val _isDiscovering = MutableLiveData<Boolean>()
    val isDiscoveringData: LiveData<Boolean>
        get() = _isDiscovering

    init {
        bluetoothManager.let {
            // Link BL Manager with system
            it.registerListeners()
            // Start listening to callbacks
            it.addBluetoothStateCallback(this)
            it.addDeviceDiscoveryCallback(this)
            it.addDiscoveryStateCallback(this)
            it.addDeviceBondCallback(this)
            it.addDeviceConnectionCallback(this)
        }

        if (bluetoothManager.isBluetoothEnabled) {
            _bluetoothState.postValue(BluetoothState.ON)
        } else {
            _bluetoothState.postValue(BluetoothState.OFF)
        }

        _isDiscovering.postValue(bluetoothManager.isInDiscovery)
    }

    // region BluetoothManager Callbacks

    override fun onBluetoothStateChanged(current: BluetoothState, previous: BluetoothState) {
        _bluetoothState.postValue(current)
    }

    override fun onDiscoveryStateChanged(isDiscovering: Boolean) {
        if (isDiscovering) {
            _deviceList.postValue(mutableListOf())
        }
        _isDiscovering.postValue(isDiscovering)
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

    fun refreshDevices(): Boolean {
        return bluetoothManager.startDiscovery()
    }

    override fun onCleared() {
        bluetoothManager.let {
            // Clear callbacks
            it.removeBluetoothStateCallback(this)
            it.removeDiscoveryStateCallback(this)
            it.removeDeviceDiscoveryCallback(this)
            it.removeDeviceBondCallback(this)
            it.removeDeviceConnectionCallback(this)
            // Clean up BL Manager
            it.unregisterReceivers()
        }

    }
}