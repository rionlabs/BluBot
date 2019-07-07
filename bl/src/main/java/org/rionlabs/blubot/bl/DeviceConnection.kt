package org.rionlabs.blubot.bl

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import timber.log.Timber
import java.io.IOException
import java.util.*

class DeviceConnection(device: BluetoothDevice) {

    private var _state: State

    private var connectThread: ConnectThread

    private var bluetoothSocket: BluetoothSocket? = null

    /**
     * Defines connection state of [DeviceConnection]
     */
    val connectionState: State
        get() = _state

    init {
        _state = State.NONE
        connectThread = ConnectThread(device).also {
            it.start()
        }
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

    private inner class ConnectThread(val device: BluetoothDevice) : Thread() {

        override fun run() {
            // Change ConnectionState
            _state = DeviceConnection.State.CONNECTING

            // Delay is added to waiting discovery to finish
            runCatching { sleep(DELAY) }

            bluetoothSocket = runCatching {
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                device.createInsecureRfcommSocketToServiceRecord(UUID.fromString(SERVER_UUID))
            }.onFailure {
                Timber.e(it, "Socket's create() method failed")
                _state = DeviceConnection.State.ERROR
                return
            }.getOrNull()

            try {
                // Connect to the remote device through the socket.
                bluetoothSocket?.connect()
                Timber.d("run: Socket connected ${if (bluetoothSocket?.isConnected == true) "true" else "false"}")
                // Socket opened, change ConnectionState
                _state = DeviceConnection.State.CONNECTED

            } catch (connectException: IOException) {
                // Unable to connect; close the socket and return.
                Timber.e(connectException, "run: ConnectException ")
                try {
                    bluetoothSocket?.close()
                } catch (closeException: IOException) {
                    Timber.e(closeException, "Could not close the client socket")
                }
                // Error, change ConnectionState
                _state = DeviceConnection.State.ERROR
                return
            }
        }
    }

    /**
     * Closes this connection.
     */
    fun close() {
        connectThread.destroy()
        _state = State.NONE
    }

    companion object {
        private const val SERVER_UUID = "00001101-0000-1000-8000-00805F9B34FB"
        private const val DELAY: Long = 3000
    }

    enum class State {
        CONNECTED,
        CONNECTING,
        NONE,
        ERROR
    }
}