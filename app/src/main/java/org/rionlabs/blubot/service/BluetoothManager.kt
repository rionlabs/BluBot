package org.rionlabs.blubot.service

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import android.bluetooth.BluetoothManager as SystemBluetoothManager

class BluetoothManager(private val appContext: Context) : LifecycleObserver {

    private var bluetoothAdapter: BluetoothAdapter? = null

    private val stateCallbackList = mutableListOf<BluetoothStateCallback>()

    val isBluetoothAvailable: Boolean
        get() {
            return bluetoothAdapter != null
        }

    val isBluetoothEnable: Boolean
        get() {
            return bluetoothAdapter?.isEnabled ?: false
        }

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

                for (callback in stateCallbackList) {
                    callback.onBluetoothStateChanged(
                        BluetoothState.fromIntReference(currentState),
                        BluetoothState.fromIntReference(previousState)
                    )
                }
            }
        }
    }

    fun addStateCallback(stateCallback: BluetoothStateCallback) {
        stateCallbackList.add(stateCallback)
    }

    fun removeStateCallback(stateCallback: BluetoothStateCallback) {
        stateCallbackList.remove(stateCallback)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onAppCreated() {
        // Initialise Bluetooth Adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppStarted() {
        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        appContext.registerReceiver(mBluetoothStateReceiver, filter)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppStopped() {
        appContext.unregisterReceiver(mBluetoothStateReceiver)
    }
}