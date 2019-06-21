package org.rionlabs.blubot.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.rionlabs.blubot.DiscoveredDevice
import org.rionlabs.blubot.databinding.ItemDiscoveredDeviceBinding as ItemBinding

class DiscoveredDeviceAdapter :
    ListAdapter<DiscoveredDevice, DiscoveredDeviceAdapter.DeviceViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return DeviceViewHolder(ItemBinding.inflate(layoutInflater, parent, false))
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun addItem(vararg discoveredDevice: DiscoveredDevice) {
        val newList = currentList.toMutableList().apply { addAll(discoveredDevice) }
        submitList(newList)
    }

    class DeviceViewHolder(private val binding: ItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(device: DiscoveredDevice) {
            binding.device = device
            binding.executePendingBindings()
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
}