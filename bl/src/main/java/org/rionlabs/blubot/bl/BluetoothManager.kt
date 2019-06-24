package org.rionlabs.blubot.bl

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import timber.log.Timber
import android.bluetooth.BluetoothManager as SystemBluetoothManager

class BluetoothManager(private val appContext: Context) {

    private var bluetoothAdapter: BluetoothAdapter? = null

    private val bluetoothStateCallbackList = mutableListOf<BluetoothStateCallback>()
    private val discoveryStateCallbackList = mutableListOf<DiscoveryStateCallback>()
    private val deviceDiscoveryCallbackList = mutableListOf<DeviceDiscoveryCallback>()
    private val deviceBondCallbackList = mutableListOf<DeviceBondCallback>()

    private val connectionMap = mutableMapOf<BluetoothDevice, DeviceConnection>()

    val isBluetoothAvailable: Boolean
        get() {
            return bluetoothAdapter != null
        }

    val isBluetoothEnable: Boolean
        get() {
            return bluetoothAdapter?.isEnabled ?: false
        }

    var isInDiscovery: Boolean = false

    var selectedDevice: BluetoothDevice? = null

    private val mBluetoothStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (BluetoothAdapter.ACTION_STATE_CHANGED == intent.action) {
                val currentState =
                    intent.getIntExtra(
                        BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR
                    )
                val previousState =
                    intent.getIntExtra(
                        BluetoothAdapter.EXTRA_PREVIOUS_STATE,
                        BluetoothAdapter.ERROR
                    )

                for (callback in bluetoothStateCallbackList) {
                    callback.onBluetoothStateChanged(
                        BluetoothState.fromIntReference(currentState),
                        BluetoothState.fromIntReference(previousState)
                    )
                }
            }
        }
    }

    private val discoveryStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action == null || action.isEmpty())
                return

            when (action) {
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    isInDiscovery = true
                    Timber.d("STATE_DISCOVERY_IN_PROGRESS")
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    isInDiscovery = false
                    Timber.d("STATE_DISCOVERY_FINISHED")
                }
                else -> {
                    Timber.w("Action : $action is not bluetooth related.")
                }
            }

            for (discoveryStateCallback in discoveryStateCallbackList) {
                discoveryStateCallback.onDiscoveryStateChanged(isInDiscovery)
            }
        }
    }

    private val deviceDiscoveryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action == null || action.isEmpty())
                return

            // When discovery finds a device
            when (action) {
                BluetoothDevice.ACTION_FOUND -> {
                    // Get the BluetoothDevice object from the Intent
                    val device =
                        intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    // Add the name and address to an array mDeviceAdapter to show in a ListView
                    if (device != null) {
                        for (deviceDiscoveryCallback in deviceDiscoveryCallbackList) {
                            deviceDiscoveryCallback.onDeviceDiscovered(device)
                        }
                        Timber.i("Discovered ${device.address}-${device.name}")
                    }
                }
                else -> {
                    Timber.w("Action : $action is not bluetooth related.")
                }
            }
        }
    }

    private val bondStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
            val bondState =
                intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_NONE)

            if (device != null) {
                when (bondState) {
                    BluetoothDevice.BOND_BONDED -> {
                        selectedDevice = device
                        for (deviceBondCallback in deviceBondCallbackList) {
                            deviceBondCallback.onConnected(device)
                        }
                    }
                    BluetoothDevice.BOND_BONDING -> {
                        for (deviceBondCallback in deviceBondCallbackList) {
                            deviceBondCallback.onConnectionStarted(device)
                        }
                    }
                    BluetoothDevice.BOND_NONE -> {
                        selectedDevice = null
                        for (deviceBondCallback in deviceBondCallbackList) {
                            deviceBondCallback.onConnectionEnded(device)
                        }
                    }
                }
            }
        }
    }

    init {
        // Initialise Bluetooth Adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    }

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

    fun startConnectionTo(bluetoothDevice: BluetoothDevice) {
        // Cancel discovery to make connection faster
        bluetoothAdapter?.cancelDiscovery()

        if (bluetoothDevice.bondState == BluetoothDevice.BOND_NONE) {
            bluetoothDevice.createBond()
        } else if (bluetoothDevice.bondState == BluetoothDevice.BOND_BONDED) {
            selectedDevice = bluetoothDevice
            for (deviceBondCallback in deviceBondCallbackList) {
                deviceBondCallback.onConnected(bluetoothDevice)
            }
        }
    }

    fun getOrStartConnection(device: BluetoothDevice): DeviceConnection {
        return connectionMap[device] ?: run {
            val connection = DeviceConnection(device)
            connectionMap[device] = connection
            connection
        }
    }

    /**
     * Needs BLUETOOTH_ADMIN permission
     */
    fun startDiscovery(): Boolean {
        val bondedDevices = bluetoothAdapter?.bondedDevices ?: mutableSetOf()
        for (bondedDevice in bondedDevices) {
            deviceDiscoveryCallbackList.forEach { it.onDeviceDiscovered(bondedDevice) }
        }

        return bluetoothAdapter?.startDiscovery() ?: false
    }

    fun start() {
        val bluetoothStateFilter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        appContext.registerReceiver(mBluetoothStateReceiver, bluetoothStateFilter)

        val discoveryStateFilter = IntentFilter()
        discoveryStateFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        discoveryStateFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        appContext.registerReceiver(discoveryStateReceiver, discoveryStateFilter)

        val deviceDiscoveryFilter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        appContext.registerReceiver(deviceDiscoveryReceiver, deviceDiscoveryFilter)

        val bondStateFilter = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        appContext.registerReceiver(bondStateReceiver, bondStateFilter)
    }

    fun stop() {
        appContext.unregisterReceiver(mBluetoothStateReceiver)
        appContext.unregisterReceiver(discoveryStateReceiver)
        appContext.unregisterReceiver(deviceDiscoveryReceiver)
        appContext.unregisterReceiver(bondStateReceiver)

        selectedDevice = null
    }
}