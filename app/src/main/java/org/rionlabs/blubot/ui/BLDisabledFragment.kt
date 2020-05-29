package org.rionlabs.blubot.ui

import android.app.Activity.RESULT_OK
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.rionlabs.blubot.databinding.FragmentBlDisabledBinding
import timber.log.Timber


class BLDisabledFragment : Fragment() {

    private lateinit var binding: FragmentBlDisabledBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBlDisabledBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.blEnableButton.setOnClickListener { requestEnableBluetooth() }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_ENABLE_BLUETOOTH) {
            if (resultCode == RESULT_OK) {
                Timber.d("onActivityResult: Bluetooth enable request succeed")
            } else {
                Timber.w("onActivityResult: Ah! User said no.")
            }
        }
    }

    private fun requestEnableBluetooth() {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivityForResult(enableBtIntent, RC_ENABLE_BLUETOOTH)
    }

    companion object {
        private const val RC_ENABLE_BLUETOOTH = 234
    }
}