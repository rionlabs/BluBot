package org.rionlabs.blubot

import android.app.Application
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import org.rionlabs.blubot.service.BluetoothManager
import timber.log.Timber

class BluBot : Application() {

    lateinit var bluetoothManager: BluetoothManager
        private set

    override fun onCreate() {
        super.onCreate()

        bluetoothManager = BluetoothManager(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_START)
            fun onStart() {
                bluetoothManager.start()
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
            fun onStop() {
                bluetoothManager.stop()
            }
        })

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}