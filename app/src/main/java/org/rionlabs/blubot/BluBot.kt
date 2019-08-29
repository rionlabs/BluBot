package org.rionlabs.blubot

import android.app.Application
import org.rionlabs.blubot.bl.BluetoothManager
import timber.log.Timber

class BluBot : Application() {

    lateinit var bluetoothManager: BluetoothManager
        private set

    override fun onCreate() {
        super.onCreate()

        bluetoothManager = BluetoothManager(this)

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}