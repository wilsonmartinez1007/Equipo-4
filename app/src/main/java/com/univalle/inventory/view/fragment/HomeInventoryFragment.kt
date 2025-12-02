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
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max
import javax.inject.Inject
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeInventoryFragment : Fragment() {

    private lateinit var binding: FragmentHomeInventoryBinding
    private val inventoryViewModel: InventoryViewModel by viewModels()

    @Inject
    lateinit var sessionManager: SessionManager
    private lateinit var adapterInventory: InventoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeInventoryBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //  Verificar sesión: si NO hay sesión, mandar a Login y cerrar esta Activity

        if (!sessionManager.isLoggedIn()) {
            startActivity(
                Intent(
                    requireContext(),
                    com.univalle.inventory.ui.login.LoginActivity::class.java
                )
            )
            requireActivity().finishAffinity()
            return
        }

        // ✅ Toolbar con título "Inventario" y botón logout
        binding.toolbarHome.toolbarInventario.title = "Inventario"
        binding.toolbarHome.btnLogout.setOnClickListener {
            // Cerrar sesión en Firebase
            FirebaseAuth.getInstance().signOut()

            // Limpiar sesión local
            sessionManager.clear()

            // 🔥 Notificar al widget que se cerró sesión
            val logoutIntent = Intent("com.univalle.inventory.LOGOUT")
            requireContext().sendBroadcast(logoutIntent)

            // Ir al login
            startActivity(
                Intent(requireContext(), com.univalle.inventory.ui.login.LoginActivity::class.java)
            )
            requireActivity().finishAffinity()
        }

        // ✅ Configurar RecyclerView y Adapter vacío al inicio
        adapterInventory = InventoryAdapter(mutableListOf(), findNavController())
        binding.recyclerViewInventario.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = adapterInventory
        }

        controladores()
        observadorViewModel()
    }

    // --------- Controladores de UI ---------
    private fun controladores() {
        // FAB → Agregar producto
        binding.fabAdd.setOnClickListener {
            findNavController().navigate(R.id.action_homeInventoryFragment_to_addItemFragment)
        }
    }

    // --------- Observadores del ViewModel ---------
    private fun observadorViewModel() {
        observerListInventory()
        observerProgress()
    }

    private fun observerListInventory() {
        // Pedir la lista (desde Firebase, vía ViewModel/Repository)
        inventoryViewModel.getListInventory()

        inventoryViewModel.listInventory.observe(viewLifecycleOwner) { listInventory ->
            // Actualizar el adapter cada vez que cambie la lista
            val adapter = InventoryAdapter(listInventory.toMutableList(), findNavController())
            binding.recyclerViewInventario.adapter = adapter
            adapter.notifyDataSetChanged()

            // Mostrar el Recycler cuando ya haya datos (aunque sea vacío)
            binding.recyclerViewInventario.isVisible = true
        }
    }

    private fun observerProgress() {
        inventoryViewModel.progressState.observe(viewLifecycleOwner) { status ->
            // Círculo de carga
            binding.progressCircular.isVisible = status
            // Mientras está cargando, ocultar el Recycler
            if (status) {
                binding.recyclerViewInventario.isVisible = false
            }
        }
    }
}