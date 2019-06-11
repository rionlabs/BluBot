package org.rionlabs.blubot.ui

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.rionlabs.blubot.BluetoothState


abstract class BluetoothActivity : AppCompatActivity() {

    protected lateinit var bluetoothAdapter: BluetoothAdapter

    protected val isBluetoothEnable: Boolean
        get() {
            return bluetoothAdapter.isEnabled
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
                onBluetoothStateChanged(
                    BluetoothState.fromIntReference(currentState),
                    BluetoothState.fromIntReference(previousState)
                )
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialise Bluetooth Adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    }

    override fun onStart() {
        super.onStart()
        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        registerReceiver(mBluetoothStateReceiver, filter)
    }

    abstract fun onBluetoothStateChanged(current: BluetoothState, previous: BluetoothState)

    override fun onStop() {
        super.onStop()
        unregisterReceiver(mBluetoothStateReceiver)
    }
}