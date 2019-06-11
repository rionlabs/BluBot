package org.rionlabs.blubot.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import org.rionlabs.blubot.databinding.FragmentBlDiscoveryBinding

class BLDiscoveryFragment : Fragment() {

    private lateinit var binding: FragmentBlDiscoveryBinding

    private val deviceAdapter = DiscoveredDeviceAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBlDiscoveryBinding.inflate(inflater, container, false).apply {
            deviceList.layoutManager = LinearLayoutManager(requireContext())
            deviceList.adapter = deviceAdapter
        }
        return binding.root
    }
}