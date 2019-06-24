package org.rionlabs.blubot.ui

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.bluetooth.BluetoothDevice
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.Uri
import android.os.Bundle
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import org.rionlabs.blubot.R
import org.rionlabs.blubot.databinding.FragmentBlDiscoveryBinding
import org.rionlabs.blubot.service.*
import org.rionlabs.blubot.ui.view.DeviceItemDecoration
import org.rionlabs.blubot.util.dataItem

class BLDiscoveryFragment : Fragment(), DiscoveredDeviceAdapter.InteractionListener,
    DiscoveryStateCallback, DeviceDiscoveryCallback, DeviceBondCallback {

    private lateinit var binding: FragmentBlDiscoveryBinding

    private val deviceAdapter = DiscoveredDeviceAdapter(this)

    private var inDiscovery: Boolean = false

    private val isLocationPermissionGranted: Boolean
        get() = checkSelfPermission(requireActivity(), ACCESS_COARSE_LOCATION) == PERMISSION_GRANTED

    private val canRequestPermission: Boolean
        get() = shouldShowRequestPermissionRationale(requireActivity(), ACCESS_COARSE_LOCATION)

    private val permissionRequiredDialog: AlertDialog
        get() = AlertDialog.Builder(requireContext())
            .setTitle(R.string.request_location_title)
            .setMessage(R.string.request_location_message)
            .setPositiveButton(android.R.string.ok) { dialogInterface: DialogInterface, _: Int ->
                dialogInterface.dismiss()
                requestLocationPermission()
            }
            .setNegativeButton(android.R.string.cancel) { dialogInterface: DialogInterface, _: Int ->
                dialogInterface.dismiss()
                toggleNoPermissionView()
            }
            .create()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBlDiscoveryBinding.inflate(inflater, container, false).apply {
            deviceList.apply {
                addItemDecoration(DeviceItemDecoration(requireContext()))
                layoutManager = LinearLayoutManager(requireContext())
                adapter = deviceAdapter
            }

            noPermissionButton.setOnClickListener {
                if ((it as Button).text == getString(R.string.open_settings)) {
                    val intent = Intent(
                        ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse(getString(R.string.package_uri, requireContext().packageName))
                    ).apply {
                        addCategory(Intent.CATEGORY_DEFAULT)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    startActivity(intent)
                } else {
                    requestLocationPermission()
                }

            }
        }
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        requireBluetoothManager().addDeviceDiscoveryCallback(this)
        requireBluetoothManager().addDiscoveryStateCallback(this)
        requireBluetoothManager().addDeviceBondCallback(this)

        if (isLocationPermissionGranted) {
            // Permission has already been granted
            discoverDevices()
        } else {
            // Permission is not granted
            if (shouldShowRequestPermissionRationale(requireActivity(), ACCESS_COARSE_LOCATION)) {
                // User has previously denied the request
                permissionRequiredDialog.show()
            } else {
                // User selected `Don't ask again`, or it is first time app launched
                requestLocationPermission()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == RC_REQUEST_LOCATION) {
            // If request is cancelled, the result arrays are empty.
            if (isLocationPermissionGranted) {
                // Permission was granted
                discoverDevices()
            }
            toggleNoPermissionView()
        }
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
        // Make the list visible
        toggleNoPermissionView()

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

        permissionRequiredDialog.apply {
            if (isShowing)
                dismiss()
        }
    }

    private fun requestLocationPermission() {
        requestPermissions(arrayOf(ACCESS_COARSE_LOCATION), RC_REQUEST_LOCATION)
    }

    private fun toggleNoPermissionView() {
        binding.apply {
            if (isLocationPermissionGranted) {
                deviceList.visibility = VISIBLE
                noPermissionGroup.visibility = GONE
            } else {
                deviceList.visibility = GONE
                noPermissionGroup.visibility = VISIBLE
                if (canRequestPermission) {
                    noPermissionButton.setText(R.string.grant_permission)
                } else {
                    noPermissionButton.setText(R.string.open_settings)
                }
            }
        }
    }

    companion object {
        const val RC_REQUEST_LOCATION = 95
    }
}