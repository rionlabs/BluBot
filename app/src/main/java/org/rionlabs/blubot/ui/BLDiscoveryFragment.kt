package org.rionlabs.blubot.ui

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import org.rionlabs.blubot.databinding.FragmentBlDiscoveryBinding
import org.rionlabs.blubot.service.DeviceDiscoveryCallback
import org.rionlabs.blubot.service.DiscoveryStateCallback
import org.rionlabs.blubot.service.requireBluetoothManager
import org.rionlabs.blubot.ui.view.DeviceItemDecoration
import org.rionlabs.blubot.util.dataItem

class BLDiscoveryFragment : Fragment(), DiscoveredDeviceAdapter.InteractionListener,
    DiscoveryStateCallback, DeviceDiscoveryCallback {

    private lateinit var binding: FragmentBlDiscoveryBinding

    private val deviceAdapter = DiscoveredDeviceAdapter(this)

    private var selectedDevice: BluetoothDevice? = null

    private var inDiscovery: Boolean = false

    private val bondStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
            val bondState =
                intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_NONE)

            if (selectedDevice != null && device.address == selectedDevice?.address) {
                when (bondState) {
                    BluetoothDevice.BOND_BONDED -> {
                        // TODO startControl
                    }
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBlDiscoveryBinding.inflate(inflater, container, false).apply {
            deviceList.addItemDecoration(DeviceItemDecoration(requireContext()))
            deviceList.layoutManager = LinearLayoutManager(requireContext())
            deviceList.adapter = deviceAdapter
        }
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        requireBluetoothManager().addDeviceDiscoveryCallback(this)
        requireBluetoothManager().addDiscoveryStateCallback(this)

        discoverDevices()

        val bondedDevices = BluetoothAdapter.getDefaultAdapter().bondedDevices
        deviceAdapter.addItem(*bondedDevices.map { it.dataItem() }.toTypedArray())
    }

    override fun onDiscoveryStateChanged(isDiscovering: Boolean) {
        inDiscovery = isDiscovering
    }

    override fun onDeviceDiscovered(bluetoothDevice: BluetoothDevice) {
        deviceAdapter.addItem(bluetoothDevice.dataItem())
    }

    private fun discoverDevices() {
        // Clear old DeviceList
        deviceAdapter.submitList(mutableListOf())

        // Start discovery
        // Needs BLUETOOTH_ADMIN permission
        val discoveryStarted = BluetoothAdapter.getDefaultAdapter().startDiscovery()
        if (!discoveryStarted)
            Toast.makeText(activity, "Bluetooth discovery failed.", Toast.LENGTH_LONG).show()
        else
            Toast.makeText(activity, "Discovery started", Toast.LENGTH_SHORT).show()
    }

    override fun onDeviceSelected(device: BluetoothDevice) {
        // Save the device in app context
        selectedDevice = device

        val filter = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        requireContext().registerReceiver(bondStateReceiver, filter)

        // Cancel discovery to make connection faster
        BluetoothAdapter.getDefaultAdapter().cancelDiscovery()

        if (device.bondState == BluetoothDevice.BOND_NONE) {
            device.createBond()
        } else if (device.bondState == BluetoothDevice.BOND_BONDED) {
            // TODO startControl
        }
    }

    // TODO add listener to refresh button in ConnectionActivity

    override fun onStop() {
        super.onStop()
        requireBluetoothManager().removeDiscoveryStateCallback(this)
        requireBluetoothManager().removeDeviceDiscoveryCallback(this)
    }
}