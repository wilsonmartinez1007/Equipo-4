package com.univalle.inventory.view.fragment

import android.content.Context
import android.os.Bundle
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.univalle.inventory.R
import com.univalle.inventory.databinding.FragmentHomeInventoryBinding
import com.univalle.inventory.viewmodel.InventoryViewModel

class HomeInventoryFragment : Fragment() {

    private var _binding: FragmentHomeInventoryBinding? = null
    private val binding get() = _binding!!

    private val inventoryViewModel: InventoryViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeInventoryBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }


}
