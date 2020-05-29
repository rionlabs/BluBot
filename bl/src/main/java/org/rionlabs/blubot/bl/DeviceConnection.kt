package org.rionlabs.blubot.bl

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import timber.log.Timber
import java.io.IOException
import java.util.*

class DeviceConnection(val device: BluetoothDevice) {

    private var _state: ConnectionState

    private var bluetoothSocket: BluetoothSocket? = null

    /**
     * Defines connection state of [DeviceConnection]
     */
    val connectionState: ConnectionState
        get() = _state

    init {
        _state = ConnectionState.NONE
    }

    fun sendSignal(signal: String): Boolean {
        return bluetoothSocket?.let {
            runCatching {
                it.outputStream.apply {
                    write(signal.toByteArray())
                    flush()
                }
                true
            }.getOrElse {
                Timber.e(it, "Failed to write signal")
                false
            }
        } ?: false
    }

    fun connect(): Boolean {
        // Change ConnectionState
        _state = ConnectionState.CONNECTING

        // Delay is added to waiting discovery to finish
        runCatching { Thread.sleep(DELAY) }

        bluetoothSocket = runCatching {
            // Get a BluetoothSocket to connect with the given BluetoothDevice.
            device.createInsecureRfcommSocketToServiceRecord(UUID.fromString(SERVER_UUID))
        }.onFailure {
            Timber.e(it, "Socket's create() method failed")
            _state = ConnectionState.ERROR
            return false
        }.getOrNull()

        try {
            // Connect to the remote device through the socket.
            bluetoothSocket?.connect()
            Timber.d("run: Socket connected ${if (bluetoothSocket?.isConnected == true) "true" else "false"}")
            // Socket opened, change ConnectionState
            _state = ConnectionState.CONNECTED
            return true

        } catch (connectException: IOException) {
            // Unable to connect; close the socket and return.
            Timber.e(connectException, "run: ConnectException ")
            try {
                bluetoothSocket?.close()
            } catch (closeException: IOException) {
                Timber.e(closeException, "Could not close the client socket")
            }
            // Error, change ConnectionState
            _state = ConnectionState.ERROR
            return false
        }
    }

    /**
     * Closes this connection.
     */
    fun close() {
        _state = ConnectionState.CONNECTING
        runCatching {

            bluetoothSocket?.close()
        }.onFailure {
            Timber.e(it, "Error while closing connection to ${bluetoothSocket?.remoteDevice?.name}")
        }
        _state = ConnectionState.NONE
    }

    companion object {
        private const val SERVER_UUID = "00001101-0000-1000-8000-00805F9B34FB"
        private const val DELAY: Long = 3000
    }
}