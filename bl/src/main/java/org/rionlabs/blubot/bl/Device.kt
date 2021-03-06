package org.rionlabs.blubot.bl

import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothDevice.*

data class Device(
    val name: String,
    val ssid: String,
    val bluetoothDevice: BluetoothDevice,
    val connectionState: ConnectionState
) {

    val deviceTypeIconRes: Int = when (bluetoothDevice.bluetoothClass.majorDeviceClass) {
        BluetoothClass.Device.Major.MISC -> R.drawable.device_class_devices_other

        BluetoothClass.Device.Major.COMPUTER -> when (bluetoothDevice.bluetoothClass.deviceClass) {
            BluetoothClass.Device.COMPUTER_LAPTOP -> R.drawable.device_class_laptop
            else -> R.drawable.device_class_desktop
        }

        BluetoothClass.Device.Major.PHONE -> R.drawable.device_class_mobile

        BluetoothClass.Device.Major.NETWORKING -> R.drawable.device_class_router

        BluetoothClass.Device.Major.AUDIO_VIDEO -> when (bluetoothDevice.bluetoothClass.deviceClass) {
            BluetoothClass.Device.AUDIO_VIDEO_LOUDSPEAKER -> R.drawable.device_class_speaker
            BluetoothClass.Device.AUDIO_VIDEO_CAR_AUDIO -> R.drawable.device_class_car
            else -> R.drawable.device_class_headset
        }

        BluetoothClass.Device.Major.PERIPHERAL -> when (bluetoothDevice.bluetoothClass.deviceClass) {
            PERIPHERAL_KEYBOARD, PERIPHERAL_KEYBOARD_POINTING -> R.drawable.device_class_keyboard
            else -> R.drawable.device_class_usb
        }

        BluetoothClass.Device.Major.IMAGING -> R.drawable.device_class_camera

        BluetoothClass.Device.Major.WEARABLE -> R.drawable.device_class_wearable

        BluetoothClass.Device.Major.TOY -> when (bluetoothDevice.bluetoothClass.deviceClass) {
            BluetoothClass.Device.TOY_CONTROLLER -> R.drawable.device_class_gamepad
            else -> R.drawable.device_class_toys
        }

        BluetoothClass.Device.Major.HEALTH -> R.drawable.device_class_health

        BluetoothClass.Device.Major.UNCATEGORIZED -> R.drawable.device_class_memory

        else -> R.drawable.device_class_unknown_device
    }

    val pairingState: PairingState = when (bluetoothDevice.bondState) {
        BOND_NONE -> PairingState.NONE
        BOND_BONDING -> PairingState.PAIRING
        BOND_BONDED -> PairingState.PAIRED
        else -> PairingState.NONE
    }

    companion object {
        const val PERIPHERAL_KEYBOARD = 0x0540
        const val PERIPHERAL_KEYBOARD_POINTING = 0x05C0
    }

    /**
     * We need to override this method, so that [pairingState] is included in comparison.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Device) return false

        if (name != other.name) return false
        if (ssid != other.ssid) return false
        if (bluetoothDevice != other.bluetoothDevice) return false
        if (connectionState != other.connectionState) return false
        if (deviceTypeIconRes != other.deviceTypeIconRes) return false
        if (pairingState != other.pairingState) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + ssid.hashCode()
        result = 31 * result + bluetoothDevice.hashCode()
        result = 31 * result + connectionState.hashCode()
        result = 31 * result + deviceTypeIconRes
        result = 31 * result + pairingState.hashCode()
        return result
    }
}