package org.rionlabs.blubot.ui

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.rionlabs.blubot.MainActivity
import timber.log.Timber

class LaunchActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = BluetoothAdapter.getDefaultAdapter()?.let {
            Intent(this, MainActivity::class.java)
        } ?: run {
            Timber.d("Bluetooth not available in device.")
            Intent(this, BluetoothNAActivity::class.java)
        }

        startActivity(intent)
        finish()
    }
}