package com.univalle.inventory.view.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.univalle.inventory.R
import com.univalle.inventory.databinding.FragmentHomeInventoryBinding
import com.univalle.inventory.utils.SessionManager
import com.univalle.inventory.view.adapter.InventoryAdapter
import com.univalle.inventory.viewmodel.InventoryViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max

class HomeInventoryFragment : Fragment() {

    private var _binding: FragmentHomeInventoryBinding? = null
    private val binding get() = _binding!!

    private val inventoryViewModel: InventoryViewModel by viewModels()
    private lateinit var adapterInventory: InventoryAdapter

    // Control del tiempo mínimo de loader (2 segundos)
    private var loadStartMs: Long = 0L
    private val minLoaderMillis = 2000L

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

        //  Verificar sesión: si NO hay sesión, mandar a Login y cerrar esta Activity
        val session = SessionManager(requireContext())
        if (!session.isLoggedIn()) {
            startActivity(
                Intent(
                    requireContext(),
                    com.univalle.inventory.ui.login.LoginActivity::class.java
                )
            )
            requireActivity().finishAffinity()
            return
        }

        // Toolbar con título "Inventario" y botón logout
        binding.toolbarHome.toolbarInventario.title = "Inventario"
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

        // Observers con espera para cumplir el mínimo de 2s de loader
        inventoryViewModel.listInventory.observe(viewLifecycleOwner) { list ->
            val elapsed = System.currentTimeMillis() - loadStartMs
            val remaining = max(0L, minLoaderMillis - elapsed)

            viewLifecycleOwner.lifecycleScope.launch {
                delay(remaining)
                adapterInventory.replaceAll(list)
                binding.progressCircular.isVisible = false
                binding.recyclerViewInventario.isVisible = list.isNotEmpty()
            }
        }

        inventoryViewModel.progressState.observe(viewLifecycleOwner) { loading ->
            if (loading) {
                binding.progressCircular.isVisible = true
                binding.recyclerViewInventario.isVisible = false
            }
        }

        // FAB → Agregar producto
        binding.fabAdd.setOnClickListener {
            findNavController().navigate(R.id.action_homeInventoryFragment_to_addItemFragment)
        }

        // Primera carga con mínimo de 2s
        startMinLoadAndFetch()
    }

    private fun startMinLoadAndFetch() {
        loadStartMs = System.currentTimeMillis()
        binding.progressCircular.isVisible = true
        binding.recyclerViewInventario.isVisible = false
        inventoryViewModel.getListInventory()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
