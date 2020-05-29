package org.rionlabs.blubot.ui

import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.rionlabs.blubot.bl.Device
import org.rionlabs.blubot.databinding.ItemDeviceBinding as ItemBinding

class DeviceAdapter(private val interactionListener: InteractionListener) :
    ListAdapter<Device, DeviceAdapter.DeviceViewHolder>(DIFF_CALLBACK) {

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return DeviceViewHolder(ItemBinding.inflate(layoutInflater, parent, false))
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val discoveredDevice = getItem(position)
        holder.bind(discoveredDevice)
        holder.onClick { interactionListener.onDeviceSelected(discoveredDevice.bluetoothDevice) }
    }

    override fun submitList(list: MutableList<Device>?) {
        super.submitList(list?.sortDistinct())
    }

    override fun submitList(list: MutableList<Device>?, commitCallback: Runnable?) {
        super.submitList(list?.sortDistinct(), commitCallback)
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).ssid.hashCode().toLong()
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

    private fun MutableList<Device>.sortDistinct(): MutableList<Device> {
        return distinct().sortedBy { device -> device.ssid }.toMutableList()
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Device>() {
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