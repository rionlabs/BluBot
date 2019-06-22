package org.rionlabs.blubot.service

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import timber.log.Timber
import android.bluetooth.BluetoothManager as SystemBluetoothManager

class BluetoothManager(private val appContext: Context) : LifecycleObserver {

    private var bluetoothAdapter: BluetoothAdapter? = null

    private val bluetoothStateCallbackList = mutableListOf<BluetoothStateCallback>()
    private val discoveryStateCallbackList = mutableListOf<DiscoveryStateCallback>()
    private val deviceDiscoveryCallbackList = mutableListOf<DeviceDiscoveryCallback>()

    val isBluetoothAvailable: Boolean
        get() {
            return bluetoothAdapter != null
        }

    val isBluetoothEnable: Boolean
        get() {
            return bluetoothAdapter?.isEnabled ?: false
        }

    var isInDiscovery: Boolean = false

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

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onAppCreated() {
        // Initialise Bluetooth Adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppStarted() {
        val bluetoothStateFilter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        appContext.registerReceiver(mBluetoothStateReceiver, bluetoothStateFilter)

        val discoveryStateFilter = IntentFilter()
        discoveryStateFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        discoveryStateFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        appContext.registerReceiver(discoveryStateReceiver, discoveryStateFilter)

        val deviceDiscoveryFilter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        appContext.registerReceiver(deviceDiscoveryReceiver, deviceDiscoveryFilter)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppStopped() {
        appContext.unregisterReceiver(mBluetoothStateReceiver)
        appContext.unregisterReceiver(discoveryStateReceiver)
        appContext.unregisterReceiver(deviceDiscoveryReceiver)
    }
}