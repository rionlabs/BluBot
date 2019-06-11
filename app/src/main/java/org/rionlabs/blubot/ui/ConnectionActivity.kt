package org.rionlabs.blubot.ui

import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.findNavController
import kotlinx.android.synthetic.main.activity_connection.*
import org.rionlabs.blubot.BluetoothState
import org.rionlabs.blubot.R
import timber.log.Timber

class ConnectionActivity : BluetoothActivity() {

    lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connection)
        setSupportActionBar(toolbar)

        navController = findNavController(R.id.connection_navigation_host)

        if (!isBluetoothEnable) {
            navController.navigate(R.id.enableBluetooth)
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