package org.rionlabs.blubot.ui.view

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import org.rionlabs.blubot.R
import org.rionlabs.blubot.databinding.ViewControlSheetBinding
import org.rionlabs.blubot.service.DeviceBondCallback
import org.rionlabs.blubot.service.bluetoothManager

class ControlSheet @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), CoordinatorLayout.AttachedBehavior,
    DeviceBondCallback {

    private val binding = ViewControlSheetBinding.inflate(LayoutInflater.from(context)).apply {
        root.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        addView(root)
    }

    private val bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onSlide(view: View, p1: Float) {
        }

        override fun onStateChanged(view: View, state: Int) {
            if (state == STATE_EXPANDED) {
                binding.navigationButton.setImageResource(R.drawable.ic_arrow_downward)
            } else if (state == STATE_COLLAPSED) {
                binding.navigationButton.setImageResource(R.drawable.ic_arrow_upward)
            }
        }
    }

    private val bottomSheetBehavior = BottomSheetBehavior<ControlSheet>(context, attrs).also {
        it.setBottomSheetCallback(bottomSheetCallback)
    }

    override fun getBehavior(): CoordinatorLayout.Behavior<*> = bottomSheetBehavior

    private var collapsedHeight: Int = 0

    init {
        binding.apply {
            navigationProgressBar.visibility = GONE
            navigationButton.visibility = GONE
            closeButton.visibility = GONE
        }

        binding.apply {
            navigationButton.setOnClickListener { changeState() }
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        collapsedHeight = binding.toolbar.height
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        bluetoothManager.addDeviceBondCallback(this)
        bluetoothManager.selectedDevice?.apply {
            bottomSheetBehavior.peekHeight = collapsedHeight
        } ?: run {
            bottomSheetBehavior.peekHeight = 0
        }
    }

    override fun onConnectionStarted(bluetoothDevice: BluetoothDevice) {
        binding.navigationButton.visibility = View.GONE
        binding.navigationProgressBar.visibility = View.VISIBLE
        binding.toolbarTitle.text = context.getString(R.string.connecting_to, bluetoothDevice.name)
        bluetoothManager.selectedDevice?.apply {
            bottomSheetBehavior.peekHeight = collapsedHeight
        }
    }

    override fun onConnected(bluetoothDevice: BluetoothDevice) {
        binding.navigationProgressBar.visibility = View.GONE
        binding.navigationButton.visibility = View.VISIBLE
        binding.closeButton.visibility = View.VISIBLE
        binding.toolbarTitle.text = context.getString(R.string.connected_to, bluetoothDevice.name)
        bluetoothManager.selectedDevice?.apply {
            bottomSheetBehavior.peekHeight = collapsedHeight
        }
    }

    override fun onConnectionEnded(bluetoothDevice: BluetoothDevice) {
        binding.apply {
            navigationProgressBar.visibility = GONE
            navigationButton.visibility = GONE
            closeButton.visibility = GONE
            binding.toolbarTitle.text = context.getString(R.string.empty)
        }
        bluetoothManager.selectedDevice?.apply {
            bottomSheetBehavior.peekHeight = 0
        }
    }

    private fun changeState() {
        if (bottomSheetBehavior.state == STATE_EXPANDED) {
            bottomSheetBehavior.state = STATE_COLLAPSED
        } else {
            bottomSheetBehavior.state = STATE_EXPANDED
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        bluetoothManager.removeDeviceBondCallback(this)
    }
}