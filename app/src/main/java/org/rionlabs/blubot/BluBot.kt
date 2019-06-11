package org.rionlabs.blubot

import android.app.Application
import timber.log.Timber

class BluBot : Application() {

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}