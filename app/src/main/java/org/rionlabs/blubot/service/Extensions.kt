package org.rionlabs.blubot.service

import android.app.Activity
import androidx.fragment.app.Fragment
import org.rionlabs.blubot.BluBot

val Activity.bluetoothManager: BluetoothManager
    get() = (application as BluBot).bluetoothManager

val Fragment.bluetoothManager: BluetoothManager?
    get() = (activity?.application as BluBot).bluetoothManager