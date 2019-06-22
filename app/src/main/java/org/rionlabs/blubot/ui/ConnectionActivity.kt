package org.rionlabs.blubot.ui

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.findNavController
import org.rionlabs.blubot.BluetoothState
import org.rionlabs.blubot.R
import org.rionlabs.blubot.databinding.ActivityConnectionBinding
import timber.log.Timber

class ConnectionActivity : BluetoothActivity() {

    private lateinit var binding: ActivityConnectionBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_connection)
        navController = findNavController(R.id.connection_navigation_host)

        if (!isBluetoothEnable) {
            navController.navigate(R.id.enableBluetooth)
        }

        binding.apply {
            aboutButton.setOnClickListener { }
            refreshButton.setOnClickListener { }
            settingsButton.setOnClickListener { }
        }
    }

    override fun onBluetoothStateChanged(current: BluetoothState, previous: BluetoothState) {
        when (current) {
            BluetoothState.OFF -> navController.navigate(R.id.enableBluetooth)
            BluetoothState.TURNING_ON -> Timber.d("State changing to ON")
            BluetoothState.ON -> navController.popBackStack()
            BluetoothState.TURNING_OFF -> Timber.d("State changing to OFF")
        }
    }
}