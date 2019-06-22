package org.rionlabs.blubot.ui.begin

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.rionlabs.blubot.service.bluetoothManager
import org.rionlabs.blubot.ui.ConnectionActivity
import timber.log.Timber

class LaunchActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = if (bluetoothManager.isBluetoothAvailable) {
            Intent(this, ConnectionActivity::class.java)
        } else {
            Timber.d("Bluetooth not available in device.")
            Intent(this, BluetoothNAActivity::class.java)
        }

        startActivity(intent)
        finish()
    }
}