package org.rionlabs.blubot.ui.view

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import org.rionlabs.blubot.R
import org.rionlabs.blubot.databinding.ViewControlSheetBinding
import org.rionlabs.blubot.service.DeviceBondCallback
import org.rionlabs.blubot.service.bluetoothManager

class ControlSheet @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), DeviceBondCallback {

    val binding = ViewControlSheetBinding.inflate(LayoutInflater.from(context)).apply {
        root.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        addView(root)
    }

    init {
        binding.apply {
            navigationProgressBar.visibility = GONE
            navigationButton.visibility = GONE
            closeButton.visibility = GONE
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        bluetoothManager.addDeviceBondCallback(this)
    }

    override fun onConnectionStarted(bluetoothDevice: BluetoothDevice) {
        binding.navigationButton.visibility = View.GONE
        binding.navigationProgressBar.visibility = View.VISIBLE
        binding.toolbarTitle.text = context.getString(R.string.connecting_to, bluetoothDevice.name)
    }

    override fun onConnected(bluetoothDevice: BluetoothDevice) {
        binding.navigationProgressBar.visibility = View.GONE
        binding.navigationButton.visibility = View.VISIBLE
        binding.closeButton.visibility = View.VISIBLE
        binding.toolbarTitle.text = context.getString(R.string.connected_to, bluetoothDevice.name)
    }

    override fun onConnectionEnded(bluetoothDevice: BluetoothDevice) {
        binding.apply {
            navigationProgressBar.visibility = GONE
            navigationButton.visibility = GONE
            closeButton.visibility = GONE
            binding.toolbarTitle.text = context.getString(R.string.empty)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        bluetoothManager.removeDeviceBondCallback(this)
    }
}