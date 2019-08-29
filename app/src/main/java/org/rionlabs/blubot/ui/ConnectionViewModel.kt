package org.rionlabs.blubot.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import org.rionlabs.blubot.service.bluetoothManager

class ConnectionViewModel(app: Application) : AndroidViewModel(app) {

    init {
        bluetoothManager.registerListeners()
    }

    override fun onCleared() {
        bluetoothManager.unregisterReceivers()
    }
}