package org.rionlabs.blubot.bl

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.IOException
import java.util.*

class DeviceConnection(val device: BluetoothDevice) {

    private var bluetoothSocket: BluetoothSocket? = null

    val isConnected: Boolean = bluetoothSocket?.isConnected ?: false

    fun sendSignal(signal: String): Boolean {
        if (!isConnected) {
            Timber.w("sendSignal: device not connected yet.")
            return false
        }

        return bluetoothSocket?.let {
            runCatching {
                it.outputStream.apply {
                    write(signal.toByteArray())
                    flush()
                }
                Timber.d("sendSignal: Signal sent")
                true
            }.getOrElse {
                Timber.d("sendSignal: Failed to write signal")
                false
            }
        } ?: false
    }

    suspend fun connect(): Boolean = withContext(Dispatchers.IO) {
        // Delay is added to waiting discovery to finish
        delay(DELAY)

        try {
            bluetoothSocket =
                device.createInsecureRfcommSocketToServiceRecord(UUID.fromString(SERVER_UUID))
                    .also {
                        it.connect()
                    }
            // Connect to the remote device through the socket.
            Timber.d("run: Socket connected ${(bluetoothSocket?.isConnected ?: false)}")
            // Socket opened, change ConnectionState

            return@withContext true

        } catch (connectException: IOException) {
            // Unable to connect; close the socket and return.
            Timber.e(connectException, "run: ConnectException ")
            try {
                bluetoothSocket?.close()
            } catch (closeException: IOException) {
                Timber.e(closeException, "Could not close the client socket")
            }
            // Error, change ConnectionState

            return@withContext false
        }
    }

    /**
     * Closes this connection.
     */
    suspend fun close() = withContext(Dispatchers.IO) {
        runCatching {
            bluetoothSocket?.close()
        }.onFailure {
            Timber.e(it, "Error while closing connection to ${bluetoothSocket?.remoteDevice?.name}")
        }
        return@withContext
    }

    companion object {
        private const val SERVER_UUID = "00001101-0000-1000-8000-00805F9B34FB"
        private const val DELAY: Long = 3000
    }
}