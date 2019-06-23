package org.rionlabs.blubot.ui

import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import org.rionlabs.blubot.databinding.FragmentBlDiscoveryBinding
import org.rionlabs.blubot.service.*
import org.rionlabs.blubot.ui.view.DeviceItemDecoration
import org.rionlabs.blubot.util.dataItem

class BLDiscoveryFragment : Fragment(), DiscoveredDeviceAdapter.InteractionListener,
    DiscoveryStateCallback, DeviceDiscoveryCallback, DeviceBondCallback {

    private lateinit var binding: FragmentBlDiscoveryBinding

    private val deviceAdapter = DiscoveredDeviceAdapter(this)

    private var inDiscovery: Boolean = false

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
        requireBluetoothManager().addDeviceBondCallback(this)

        discoverDevices()
    }

    override fun onDiscoveryStateChanged(isDiscovering: Boolean) {
        inDiscovery = isDiscovering
    }

    override fun onDeviceDiscovered(bluetoothDevice: BluetoothDevice) {
        deviceAdapter.addItem(bluetoothDevice.dataItem())
    }

    override fun onConnectionStarted(bluetoothDevice: BluetoothDevice) {
        // TODO To change body of created functions
    }

    override fun onConnected(bluetoothDevice: BluetoothDevice) {
        deviceAdapter.notifyDataSetChanged()
    }

    override fun onConnectionEnded(bluetoothDevice: BluetoothDevice) {
        deviceAdapter.notifyDataSetChanged()
    }

    private fun discoverDevices() {
        // Clear old DeviceList
        deviceAdapter.submitList(mutableListOf())

        // Start discovery
        val discoveryStarted = bluetoothManager?.startDiscovery() ?: false
        if (!discoveryStarted)
            Toast.makeText(activity, "Bluetooth discovery failed.", Toast.LENGTH_LONG).show()
        else
            Toast.makeText(activity, "Discovery started", Toast.LENGTH_SHORT).show()
    }

    override fun onDeviceSelected(device: BluetoothDevice) {
        bluetoothManager?.startConnectionTo(device)
    }

    override fun onStop() {
        super.onStop()
        requireBluetoothManager().removeDiscoveryStateCallback(this)
        requireBluetoothManager().removeDeviceDiscoveryCallback(this)
        requireBluetoothManager().removeDeviceBondCallback(this)
    }
}