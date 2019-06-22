package org.rionlabs.blubot.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import org.rionlabs.blubot.R
import org.rionlabs.blubot.databinding.ActivityConnectionBinding
import org.rionlabs.blubot.service.BluetoothState
import org.rionlabs.blubot.service.BluetoothStateCallback
import org.rionlabs.blubot.service.bluetoothManager
import timber.log.Timber

class ConnectionActivity : AppCompatActivity(), BluetoothStateCallback {

    private lateinit var binding: ActivityConnectionBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_connection)
        navController = findNavController(R.id.connection_navigation_host)

        bluetoothManager.addBluetoothStateCallback(this)

        if (!bluetoothManager.isBluetoothEnable) {
            navController.navigate(R.id.enableBluetooth)
        }

        val behavior = BottomSheetBehavior.from(binding.controlSheet)

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

    override fun onDestroy() {
        super.onDestroy()
        bluetoothManager.removeBluetoothStateCallback(this)
    }
}