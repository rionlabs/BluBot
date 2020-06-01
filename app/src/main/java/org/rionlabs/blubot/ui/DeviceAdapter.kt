package org.rionlabs.blubot.ui

import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.rionlabs.blubot.R
import org.rionlabs.blubot.bl.Device
import timber.log.Timber
import org.rionlabs.blubot.databinding.ItemDeviceBinding as ItemBinding
import org.rionlabs.blubot.databinding.ItemDeviceHeaderBinding as ItemHeaderBinding

class DeviceAdapter(private val interactionListener: InteractionListener) :
    ListAdapter<Device, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    private var isScanning: Boolean = false

    init {
        setHasStableIds(true)
    }

    fun updateScanningMode(isScanning: Boolean) {
        Timber.d("updateScanningMode() called with: isScanning = [$isScanning]")
        if (this.isScanning != isScanning) {
            this.isScanning = isScanning
            notifyItemChanged(HEADER_POSITION)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            R.layout.item_device_header ->
                HeaderViewHolder(ItemHeaderBinding.inflate(layoutInflater, parent, false))
            else -> {
                DeviceViewHolder(ItemBinding.inflate(layoutInflater, parent, false))
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            R.layout.item_device_header -> {
                (holder as HeaderViewHolder).bind(isScanning)
            }
            else -> {
                val discoveredDevice = getItem(position)
                with(holder as DeviceViewHolder) {
                    bind(discoveredDevice)
                    onClick { interactionListener.onDeviceSelected(discoveredDevice.bluetoothDevice) }
                }
            }
        }
    }

    override fun getItemId(position: Int): Long {
        return if (position == 0) {
            -1
        } else
            with(getItem(position - 1)) {
                (ssid + name).hashCode().toLong()
            }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == HEADER_POSITION) {
            R.layout.item_device_header
        } else {
            R.layout.item_device
        }
    }

    override fun submitList(list: MutableList<Device>?) {
        super.submitList(list?.sortDistinct())
    }

    override fun submitList(list: MutableList<Device>?, commitCallback: Runnable?) {
        super.submitList(list?.sortDistinct(), commitCallback)
    }

    class DeviceViewHolder(private val binding: ItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(device: Device) {
            if (device.name.isEmpty()) {
                binding.device = device.copy(name = device.ssid)
                binding.deviceSsidText.visibility = GONE
            } else {
                binding.device = device
            }
            binding.executePendingBindings()
        }

        fun onClick(block: (() -> Unit)) {
            binding.root.setOnClickListener { block() }
        }
    }

    class HeaderViewHolder(private val binding: ItemHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(isScanning: Boolean) {
            binding.isScanning = isScanning
            binding.executePendingBindings()
        }
    }

    private fun MutableList<Device>.sortDistinct(): MutableList<Device> {
        return distinct().sortedBy { device -> device.ssid }.toMutableList()
    }

    companion object {

        private const val HEADER_POSITION = 0

        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Device>() {
            override fun areItemsTheSame(old: Device, new: Device): Boolean {
                return old.ssid == new.ssid
            }

            override fun areContentsTheSame(old: Device, new: Device): Boolean {
                return old == new
            }
        }
    }

    interface InteractionListener {
        fun onDeviceSelected(device: BluetoothDevice)
    }
}