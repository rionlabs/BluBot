package org.rionlabs.blubot

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import org.rionlabs.blubot.service.BluetoothManager
import timber.log.Timber

class BluBot : Application() {

    lateinit var bluetoothManager: BluetoothManager
        private set

    override fun onCreate() {
        super.onCreate()

        bluetoothManager = BluetoothManager(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(bluetoothManager)

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}