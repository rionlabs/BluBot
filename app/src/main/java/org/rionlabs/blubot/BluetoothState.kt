package org.rionlabs.blubot

import android.bluetooth.BluetoothAdapter

enum class BluetoothState {
    OFF,
    TURNING_ON,
    ON,
    TURNING_OFF;

    companion object {
        fun fromIntReference(stateInt: Int): BluetoothState {
            return when (stateInt) {
                BluetoothAdapter.STATE_OFF -> OFF
                BluetoothAdapter.STATE_TURNING_ON -> TURNING_ON
                BluetoothAdapter.STATE_ON -> ON
                BluetoothAdapter.STATE_TURNING_OFF -> TURNING_OFF
                else ->
                    throw IllegalStateException("Enum must correspond to BluetoothAdapter state int")
            }
        }
    }
}