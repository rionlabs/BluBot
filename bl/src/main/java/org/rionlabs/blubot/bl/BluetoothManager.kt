package org.rionlabs.blubot.bl

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import org.rionlabs.blubot.bl.callback.CallbackManager
import timber.log.Timber

class BluetoothManager(private val appContext: Context) : CallbackManager() {

    private var bluetoothAdapter: BluetoothAdapter? = null

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
                            deviceDiscoveryCallback.onDeviceDiscovered(device.dataItem())
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
                            deviceBondCallback.onConnected(device.dataItem())
                        }
                    }
                    BluetoothDevice.BOND_BONDING -> {
                        for (deviceBondCallback in deviceBondCallbackList) {
                            deviceBondCallback.onConnectionStarted(device.dataItem())
                        }
                    }
                    BluetoothDevice.BOND_NONE -> {
                        selectedDevice = null
                        for (deviceBondCallback in deviceBondCallbackList) {
                            deviceBondCallback.onConnectionEnded(device.dataItem())
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

    fun startConnectionTo(bluetoothDevice: BluetoothDevice) {
        // Cancel discovery to make connection faster
        bluetoothAdapter?.cancelDiscovery()

        if (bluetoothDevice.bondState == BluetoothDevice.BOND_NONE) {
            bluetoothDevice.createBond()
        } else if (bluetoothDevice.bondState == BluetoothDevice.BOND_BONDED) {
            selectedDevice = bluetoothDevice
            for (deviceBondCallback in deviceBondCallbackList) {
                deviceBondCallback.onConnected(bluetoothDevice.dataItem())
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
            deviceDiscoveryCallbackList.forEach { it.onDeviceDiscovered(bondedDevice.dataItem()) }
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