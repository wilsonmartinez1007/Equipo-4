package com.univalle.inventory.view.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.univalle.inventory.R
import com.univalle.inventory.databinding.FragmentHomeInventoryBinding
import com.univalle.inventory.utils.SessionManager
import com.univalle.inventory.view.adapter.InventoryAdapter
import com.univalle.inventory.viewmodel.InventoryViewModel

class HomeInventoryFragment : Fragment() {

    private var _binding: FragmentHomeInventoryBinding? = null
    private val binding get() = _binding!!

    private val inventoryViewModel: InventoryViewModel by viewModels()
    private lateinit var adapterInventory: InventoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeInventoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Toolbar (incluida desde toolbar_home.xml)
        binding.toolbarHome.toolbarInventario.title = getString(R.string.app_name)
        binding.toolbarHome.btnLogout.setOnClickListener {
            SessionManager(requireContext()).clear()
            startActivity(
                Intent(requireContext(), com.univalle.inventory.ui.login.LoginActivity::class.java)
            )
            requireActivity().finishAffinity()
        }

        // Recycler + Adapter
        adapterInventory = InventoryAdapter(mutableListOf(), findNavController())
        binding.recyclerViewInventario.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = adapterInventory
        }

        // Observers
        inventoryViewModel.listInventory.observe(viewLifecycleOwner) { list ->
            adapterInventory.replaceAll(list)
            binding.recyclerViewInventario.isVisible = list.isNotEmpty()
        }

        inventoryViewModel.progressState.observe(viewLifecycleOwner) { loading ->
            binding.progressCircular.isVisible = loading
            if (loading) binding.recyclerViewInventario.isVisible = false
        }

        // FAB → Agregar producto
        binding.fabAdd.setOnClickListener {
            findNavController().navigate(R.id.action_homeInventoryFragment_to_addItemFragment)
        }

        // Primera carga
        inventoryViewModel.getListInventory()
    }

    override fun onResume() {
        super.onResume()
        inventoryViewModel.getListInventory()

        // Ajuste defensivo de visibilidad por si el observer aún no disparó
        val hasItems = ::adapterInventory.isInitialized && adapterInventory.itemCount > 0
        binding.recyclerViewInventario.isVisible = hasItems
        binding.progressCircular.isVisible = false
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
