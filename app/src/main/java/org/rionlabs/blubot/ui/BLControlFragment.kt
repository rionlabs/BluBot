package org.rionlabs.blubot.ui

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import timber.log.Timber
import java.io.IOException
import java.util.*

class BLControlFragment {

    private lateinit var mConnectThread: ConnectThread

    private var mSocket: BluetoothSocket? = null

// Intialize
//        controlBoard.setOnButtonClickListener { button ->
//            mSocket?.let {
//                runCatching {
//                    it.outputStream.apply {
//                        write(ByteArray(button.signal))
//                        flush()
//                    }
//                }.onFailure {
//                    Timber.e(it, "Failed to write signal")
//                }
//            }
//        }
//        mConnectThread = ConnectThread(connectedDevice).also {
//            it.start()
//        }

    fun setSocket(socket: BluetoothSocket?) {
        mSocket = socket
    }

    private inner class ConnectThread(device: BluetoothDevice) : Thread() {

        private val mmSocket: BluetoothSocket? = runCatching {
            // Get a BluetoothSocket to connect with the given BluetoothDevice.
            device.createInsecureRfcommSocketToServiceRecord(UUID.fromString(SERVER_UUID))
        }.onFailure {
            Timber.e(it, "Socket's create() method failed")
        }.getOrNull()

        override fun run() {
            try {
                sleep(DELAY)
            } catch (ignored: InterruptedException) {
            }

            try {
                // Connect to the remote device through the socket.
                mmSocket?.connect()
                Timber.d("run: Socket connected ${if (mmSocket?.isConnected == true) "true" else "false"}")
            } catch (connectException: IOException) {
                // Unable to connect; close the socket and return.
                Timber.e(connectException, "run: ConnectException ")
                try {
                    mmSocket?.close()
                } catch (closeException: IOException) {
                    Timber.e(closeException, "Could not close the client socket")
                }
                return
            }

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
//            runOnUiThread {
//                setSocket(mmSocket)
//            }
        }

        /**
         * Closes the client socket and causes the thread to finish.
         */
        fun cancel() {
            try {
                mmSocket?.close()
            } catch (e: IOException) {
                Timber.e(e, "Could not close the client socket")
            }

        }
    }
// OnDestroy
//        mConnectThread.cancel()

    companion object {
        private const val SERVER_UUID = "00001101-0000-1000-8000-00805F9B34FB"
        private const val DELAY: Long = 3000
    }
}