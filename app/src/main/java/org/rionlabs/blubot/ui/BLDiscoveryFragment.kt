package org.rionlabs.blubot.ui

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import org.rionlabs.blubot.R
import org.rionlabs.blubot.databinding.FragmentBlDiscoveryBinding
import org.rionlabs.blubot.ui.view.DeviceItemDecoration
import org.rionlabs.blubot.util.dataItem
import timber.log.Timber

class BLDiscoveryFragment : Fragment(), DiscoveredDeviceAdapter.InteractionListener {

    private lateinit var binding: FragmentBlDiscoveryBinding

    private val deviceAdapter = DiscoveredDeviceAdapter(this)

    private var selectedDevice: BluetoothDevice? = null

    private var inDiscovery: Boolean = false

    // Create a BroadcastReceiver for discovery of bluetooth mDeviceList
    private val bluetoothReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action == null || action.isEmpty())
                return

            // When discovery finds a device
            when (action) {
                BluetoothDevice.ACTION_FOUND -> {
                    // Get the BluetoothDevice object from the Intent
                    val device =
                        intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    // Add the name and address to an array mDeviceAdapter to show in a ListView
                    if (device != null) {
                        deviceAdapter.addItem(device.dataItem())
                        Timber.i("${device.address}-${device.name}")
                    }
                }
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    inDiscovery = true
                    Timber.d("STATE_DISCOVERY_IN_PROGRESS")
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    inDiscovery = false
                    Timber.d("STATE_DISCOVERY_FINISHED")
                }
                else -> {
                    Timber.w("Action : $action is not bluetooth related.")
                }
            }
        }
    }

    private val bondStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
            val bondState =
                intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_NONE)

            if (selectedDevice != null && device.address == selectedDevice?.address) {
                when (bondState) {
                    BluetoothDevice.BOND_BONDED -> {
                        val startControl = BLDiscoveryFragmentDirections.startControl(device)
                        findNavController().navigate(startControl)
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

        // Register the BroadcastReceiver
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        requireContext().registerReceiver(bluetoothReceiver, filter)

        discoverDevices()

        val bondedDevices = BluetoothAdapter.getDefaultAdapter().bondedDevices
        deviceAdapter.addItem(*bondedDevices.map { it.dataItem() }.toTypedArray())
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
            val startControl = BLDiscoveryFragmentDirections.startControl(device)
            findNavController().navigate(startControl)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.discovery, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.action_refresh && inDiscovery.not()) {
            discoverDevices()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStop() {
        super.onStop()
        requireContext().unregisterReceiver(bluetoothReceiver)
    }
}