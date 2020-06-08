package org.rionlabs.blubot.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
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

    private val _deviceMap = MutableLiveData<MutableMap<String, Device>>()
    val deviceListData: LiveData<List<Device>>
        get() = _deviceMap.map { it?.values?.toList() ?: emptyList() }

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
            _deviceMap.postValue(mutableMapOf())
        }
        _isDiscovering.postValue(isDiscovering)
    }

    override fun onDeviceDiscovered(device: Device) = updateMap(device)

    override fun onBondStarted(device: Device) = updateMap(device)

    override fun onBonded(device: Device) = updateMap(device)

    override fun onBondEnded(device: Device) = updateMap(device)

    override fun onConnectionStateChanged(device: Device) = updateMap(device)

    // endregion

    private fun updateMap(newDevice: Device) {
        val deviceList = _deviceMap.value ?: mutableMapOf()
        deviceList[newDevice.ssid] = newDevice
        _deviceMap.postValue(deviceList)
    }

    fun refreshDevices(): Boolean {
        return bluetoothManager.startDiscovery()
    }

    fun stopDiscovery(): Boolean {
        return bluetoothManager.stopDiscovery()
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