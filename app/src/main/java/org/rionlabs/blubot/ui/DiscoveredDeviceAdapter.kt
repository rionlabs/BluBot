package org.rionlabs.blubot.ui

import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.rionlabs.blubot.DiscoveredDevice
import org.rionlabs.blubot.databinding.ItemDiscoveredDeviceBinding as ItemBinding

class DiscoveredDeviceAdapter(private val interactionListener: InteractionListener) :
    ListAdapter<DiscoveredDevice, DiscoveredDeviceAdapter.DeviceViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return DeviceViewHolder(ItemBinding.inflate(layoutInflater, parent, false))
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val discoveredDevice = getItem(position)
        holder.bind(discoveredDevice)
        holder.onClick { interactionListener.onDeviceSelected(discoveredDevice.bluetoothDevice) }
    }

    override fun submitList(list: MutableList<DiscoveredDevice>?) {
        super.submitList(list?.distinct())
    }

    fun addItem(vararg discoveredDevice: DiscoveredDevice) {
        val newList = currentList.toMutableList().apply { addAll(discoveredDevice) }
        submitList(newList)
    }

    class DeviceViewHolder(private val binding: ItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(device: DiscoveredDevice) {
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

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<DiscoveredDevice>() {
            override fun areItemsTheSame(old: DiscoveredDevice, new: DiscoveredDevice): Boolean {
                return old.ssid == new.ssid
            }

            override fun areContentsTheSame(old: DiscoveredDevice, new: DiscoveredDevice): Boolean {
                return old == new
            }
        }
    }

    interface InteractionListener {
        fun onDeviceSelected(device: BluetoothDevice)
    }
}