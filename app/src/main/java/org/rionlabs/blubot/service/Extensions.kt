package org.rionlabs.blubot.service

import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import org.rionlabs.blubot.BluBot
import org.rionlabs.blubot.bl.BluetoothManager

val AndroidViewModel.bluetoothManager: BluetoothManager
    get() = getApplication<BluBot>().bluetoothManager

val Fragment.bluetoothManager: BluetoothManager?
    get() = (activity?.application as BluBot).bluetoothManager

fun Fragment.requireBluetoothManager(): BluetoothManager =
    (requireActivity().application as BluBot).bluetoothManager

val View.bluetoothManager: BluetoothManager
    get() = (context.applicationContext as BluBot).bluetoothManager