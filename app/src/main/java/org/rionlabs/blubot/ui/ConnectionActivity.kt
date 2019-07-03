package org.rionlabs.blubot.ui

import android.os.Bundle
import android.view.View.*
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.findNavController
import org.rionlabs.blubot.R
import org.rionlabs.blubot.bl.BluetoothState
import org.rionlabs.blubot.bl.callback.BluetoothStateCallback
import org.rionlabs.blubot.bl.callback.DiscoveryStateCallback
import org.rionlabs.blubot.databinding.ActivityConnectionBinding
import org.rionlabs.blubot.service.bluetoothManager
import timber.log.Timber

class ConnectionActivity : AppCompatActivity(),
    BluetoothStateCallback,
    DiscoveryStateCallback {

    private lateinit var binding: ActivityConnectionBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_connection)
        navController = findNavController(R.id.connection_navigation_host)

        bluetoothManager.addBluetoothStateCallback(this)
        bluetoothManager.addDiscoveryStateCallback(this)

        if (!bluetoothManager.isBluetoothEnable) {
            binding.refreshButton.visibility = INVISIBLE
            navController.navigate(R.id.enableBluetooth)
        } else {
            binding.refreshButton.visibility = VISIBLE
        }

        binding.apply {
            aboutButton.setOnClickListener { }
            refreshButton.setOnClickListener { bluetoothManager.startDiscovery() }
            settingsButton.setOnClickListener { }
        }

        toggleRefreshMenu(bluetoothManager.isInDiscovery)
    }

    override fun onBluetoothStateChanged(current: BluetoothState, previous: BluetoothState) {
        when (current) {
            BluetoothState.OFF -> {
                navController.navigate(R.id.enableBluetooth)
                binding.refreshButton.visibility = INVISIBLE
            }
            BluetoothState.TURNING_ON -> {
                Timber.d("State changing to ON")
            }
            BluetoothState.ON -> {
                navController.popBackStack()
                binding.refreshButton.visibility = VISIBLE
            }
            BluetoothState.TURNING_OFF -> {
                Timber.d("State changing to OFF")
            }
        }
    }

    override fun onDiscoveryStateChanged(isDiscovering: Boolean) {
        toggleRefreshMenu(isDiscovering)
    }

    private fun toggleRefreshMenu(isDiscovering: Boolean) {
        binding.apply {
            if (isDiscovering) {
                refreshButton.visibility = GONE
                refreshProgressBar.visibility = VISIBLE
            } else {
                refreshButton.visibility = VISIBLE
                refreshProgressBar.visibility = GONE
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bluetoothManager.removeBluetoothStateCallback(this)
        bluetoothManager.removeDiscoveryStateCallback(this)
    }
}