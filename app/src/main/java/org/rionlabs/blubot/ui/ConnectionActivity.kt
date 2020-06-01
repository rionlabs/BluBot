package org.rionlabs.blubot.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.commit
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import org.rionlabs.blubot.R
import org.rionlabs.blubot.bl.BluetoothState
import org.rionlabs.blubot.databinding.ActivityConnectionBinding
import timber.log.Timber

class ConnectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConnectionBinding
    private lateinit var viewModel: ConnectionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_connection)
        viewModel = ViewModelProvider(this).get(ConnectionViewModel::class.java)

        binding.apply {
            scanButton.setOnClickListener {
                if (scanButton.text == getString(R.string.scan)) {
                    // Scan is stopped
                    viewModel.refreshDevices()
                } else {
                    // Scanning in progress
                    viewModel.stopDiscovery()
                }
            }
            settingsButton.setOnClickListener {

            }
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.bluetoothStateData.observe(this, Observer {
            when (it ?: return@Observer) {
                BluetoothState.OFF -> {
                    supportFragmentManager.commit {
                        replace(R.id.fragmentContainer, BLDisabledFragment())
                    }
                    binding.scanButton.hide()
                }
                BluetoothState.TURNING_ON -> {
                    Timber.d("State changing to ON")
                }
                BluetoothState.ON -> {
                    supportFragmentManager.commit {
                        replace(R.id.fragmentContainer, BLDiscoveryFragment())
                    }
                    binding.scanButton.show()
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
                scanButton.setIconResource(R.drawable.ic_action_stop)
                scanButton.setText(R.string.stop)
            } else {
                scanButton.setIconResource(R.drawable.ic_action_refresh)
                scanButton.setText(R.string.scan)
            }
        }
    }
}