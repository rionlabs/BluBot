package org.rionlabs.blubot.ui

import android.Manifest.permission.ACCESS_FINE_LOCATION
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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import org.rionlabs.blubot.BuildConfig
import org.rionlabs.blubot.R
import org.rionlabs.blubot.bl.callback.DiscoveryStateCallback
import org.rionlabs.blubot.databinding.FragmentBlDiscoveryBinding
import org.rionlabs.blubot.service.bluetoothManager
import org.rionlabs.blubot.service.requireBluetoothManager
import org.rionlabs.blubot.ui.view.DeviceItemDecoration

class BLDiscoveryFragment : Fragment(), DeviceAdapter.InteractionListener,
    DiscoveryStateCallback {

    private lateinit var binding: FragmentBlDiscoveryBinding

    private lateinit var viewModel: ConnectionViewModel

    private val deviceAdapter = DeviceAdapter(this)

    private var inDiscovery: Boolean = false

    private val isLocationPermissionGranted: Boolean
        get() = checkSelfPermission(requireActivity(), ACCESS_FINE_LOCATION) == PERMISSION_GRANTED

    private val canRequestPermission: Boolean
        get() = shouldShowRequestPermissionRationale(requireActivity(), ACCESS_FINE_LOCATION)

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(ConnectionViewModel::class.java)
    }

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
        requireBluetoothManager().addDiscoveryStateCallback(this)

        viewModel.deviceListData.observe(viewLifecycleOwner, Observer {
            val deviceList = it.toMutableList()
            deviceAdapter.submitList(deviceList)
        })

        if (isLocationPermissionGranted) {
            // Permission has already been granted
            discoverDevices()
        } else {
            // Permission is not granted
            if (shouldShowRequestPermissionRationale(requireActivity(), ACCESS_FINE_LOCATION)) {
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

    private fun discoverDevices() {
        // Make the list visible
        toggleNoPermissionView()

        // Clear old DeviceList
        deviceAdapter.submitList(mutableListOf())

        // Start discovery
        val discoveryStarted = bluetoothManager?.startDiscovery() ?: false
        if (!discoveryStarted && BuildConfig.DEBUG)
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

        permissionRequiredDialog.apply {
            if (isShowing)
                dismiss()
        }
    }

    private fun requestLocationPermission() {
        requestPermissions(arrayOf(ACCESS_FINE_LOCATION), RC_REQUEST_LOCATION)
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