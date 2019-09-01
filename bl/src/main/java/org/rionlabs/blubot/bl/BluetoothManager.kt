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

    /**
     * True if the current device supports Bluetooth.
     * Doesn't rely on [registerListeners] or [unregisterReceivers].
     */
    val isBluetoothAvailable: Boolean
        get() {
            return bluetoothAdapter != null
        }

    /**
     * True if bluetooth is enabled.
     * Doesn't rely on [registerListeners] or [unregisterReceivers].
     */
    val isBluetoothEnabled: Boolean
        get() {
            return bluetoothAdapter?.isEnabled ?: false
        }

    /**
     * True if the [BluetoothManager] instance is discovering new devices.
     */
    var isInDiscovery: Boolean = false

    /**
     * Selected device.
     */
    var selectedDevice: BluetoothDevice? = null

    // region Bluetooth Receivers

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
                            deviceBondCallback.onBonded(device.dataItem())
                        }
                    }
                    BluetoothDevice.BOND_BONDING -> {
                        for (deviceBondCallback in deviceBondCallbackList) {
                            deviceBondCallback.onBondStarted(device.dataItem())
                        }
                    }
                    BluetoothDevice.BOND_NONE -> {
                        selectedDevice = null
                        for (deviceBondCallback in deviceBondCallbackList) {
                            deviceBondCallback.onBondEnded(device.dataItem())
                        }
                    }
                }
            }
        }
    }

    // endregion

    init {
        // Initialise Bluetooth Adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    }

    /**
     * Registers [BroadcastReceiver]s for listening bluetooth events. Most of the methods depend of this method.
     * Need to call [unregisterReceivers] to clear the receivers.
     */
    fun registerListeners() {
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

    // region Connection Methods

    fun startConnectionTo(bluetoothDevice: BluetoothDevice) {
        // Cancel discovery to make connection faster
        bluetoothAdapter?.cancelDiscovery()

        if (bluetoothDevice.bondState == BluetoothDevice.BOND_NONE) {
            selectedDevice = bluetoothDevice
            bluetoothDevice.createBond()
            for (deviceBondCallback in deviceBondCallbackList) {
                deviceBondCallback.onBondStarted(bluetoothDevice.dataItem())
            }
        } else if (bluetoothDevice.bondState == BluetoothDevice.BOND_BONDED) {
            for (deviceBondCallback in deviceBondCallbackList) {
                deviceBondCallback.onBonded(bluetoothDevice.dataItem())
            }
            // If already bonded, then start connection
            val connection = DeviceConnection(bluetoothDevice)
            connection.connect()
            for (connectionCallback in deviceConnectionCallbackList) {
                connectionCallback.onConnectionStateChanged(bluetoothDevice.dataItem())
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

    fun closeConnection(device: BluetoothDevice) {
        connectionMap[device]?.close()
    }

    //endregion

    /**
     * Needs [android.Manifest.permission.BLUETOOTH_ADMIN] permission
     */
    fun startDiscovery(): Boolean {
        val bondedDevices = bluetoothAdapter?.bondedDevices ?: mutableSetOf()
        for (bondedDevice in bondedDevices) {
            deviceDiscoveryCallbackList.forEach { it.onDeviceDiscovered(bondedDevice.dataItem()) }
        }

        return bluetoothAdapter?.startDiscovery() ?: false
    }

    /**
     * Unregisters the receivers. No callback will work after this method called.
     */
    fun unregisterReceivers() {
        appContext.unregisterReceiver(mBluetoothStateReceiver)
        appContext.unregisterReceiver(discoveryStateReceiver)
        appContext.unregisterReceiver(deviceDiscoveryReceiver)
        appContext.unregisterReceiver(bondStateReceiver)

        selectedDevice = null
    }
}