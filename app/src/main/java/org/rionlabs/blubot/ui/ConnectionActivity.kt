package org.rionlabs.blubot.ui

import android.os.Bundle
import android.view.View.*
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.findNavController
import org.rionlabs.blubot.R
import org.rionlabs.blubot.bl.BluetoothState
import org.rionlabs.blubot.databinding.ActivityConnectionBinding
import timber.log.Timber

class ConnectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConnectionBinding
    private lateinit var viewModel: ConnectionViewModel
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_connection)
        viewModel = ViewModelProviders.of(this).get(ConnectionViewModel::class.java)
        navController = findNavController(R.id.connection_navigation_host)

        binding.apply {
            aboutButton.setOnClickListener { }
            refreshButton.setOnClickListener { viewModel.refreshDevices() }
            settingsButton.setOnClickListener { }
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.bluetoothStateData.observe(this, Observer {
            when (it ?: return@Observer) {
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
        })

        viewModel.isDiscoveringData.observe(this, Observer {
            toggleRefreshMenu(it)
        })
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
}