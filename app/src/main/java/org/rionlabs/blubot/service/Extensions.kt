package org.rionlabs.blubot.service

import android.app.Activity
import android.view.View
import androidx.fragment.app.Fragment
import org.rionlabs.blubot.BluBot

val Activity.bluetoothManager: BluetoothManager
    get() = (application as BluBot).bluetoothManager

val Fragment.bluetoothManager: BluetoothManager?
    get() = (activity?.application as BluBot).bluetoothManager

fun Fragment.requireBluetoothManager(): BluetoothManager =
    (requireActivity().application as BluBot).bluetoothManager

val View.bluetoothManager: BluetoothManager
    get() = (context.applicationContext as BluBot).bluetoothManager