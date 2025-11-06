package com.univalle.inventory.view.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.univalle.inventory.R
import com.univalle.inventory.data.InventoryDB
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.widget.TextView
import com.univalle.inventory.model.Inventory
import java.text.NumberFormat
import java.util.*

class ProductDetailFragment : Fragment() {

    private var productId: Int = -1
    private lateinit var db: InventoryDB

    private lateinit var tvName: TextView
    private lateinit var tvPrice: TextView
    private lateinit var tvQuantity: TextView
    private lateinit var tvTotal: TextView
    private lateinit var btnDelete: MaterialButton
    private lateinit var fabEdit: FloatingActionButton
    private lateinit var toolbar: MaterialToolbar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_product_detail, container, false)

        db = InventoryDB.getDatabase(requireContext())

        // Referencias UI
        tvName = view.findViewById(R.id.tvName)
        tvPrice = view.findViewById(R.id.tvPrice)
        tvQuantity = view.findViewById(R.id.tvQuantity)
        tvTotal = view.findViewById(R.id.tvTotal)
        btnDelete = view.findViewById(R.id.btnDelete)
        fabEdit = view.findViewById(R.id.fabEdit)
        toolbar = view.findViewById(R.id.toolbarDetail)

        // Obtener argumentos
        arguments?.let {
            productId = it.getInt("productId", -1)
        }

        // Configurar Toolbar
        toolbar.setNavigationOnClickListener {
            // TODO: Navegar a HU 3.0 (Ventana Home Inventario)
            findNavController().popBackStack()
        }

        // Cargar producto desde Room
        if (productId != -1) {
            loadProductDetail()
        }

        // Eliminar producto
        btnDelete.setOnClickListener {
            showDeleteDialog()
        }

        // Editar producto
        fabEdit.setOnClickListener {
            // TODO: Navegar a HU 6.0 (Ventana Editar Producto)
            // findNavController().navigate(R.id.action_detail_to_edit, bundleOf("productId" to productId))
        }

        return view
    }

    private fun loadProductDetail() {
        lifecycleScope.launch(Dispatchers.IO) {
            val product = db.inventoryDao().getInventoryById(productId)
            product?.let {
                withContext(Dispatchers.Main) { bindProductData(it) }
            }
        }
    }

    private fun bindProductData(product: Inventory) {
        val formatoColombiano = NumberFormat.getNumberInstance(Locale("es", "CO"))
        formatoColombiano.maximumFractionDigits = 2
        formatoColombiano.minimumFractionDigits = 2

        tvName.text = product.name
        tvPrice.text = "Precio unidad: $${formatoColombiano.format(product.price)}"
        tvQuantity.text = "Cantidad disponible: ${product.quantity}"
        tvTotal.text = "Total: $${formatoColombiano.format(product.price * product.quantity)}"
    }

    private fun showDeleteDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Confirmar eliminación")
            .setMessage("¿Deseas eliminar este producto?")
            .setNegativeButton("No", null)
            .setPositiveButton("Sí") { _, _ ->
                lifecycleScope.launch(Dispatchers.IO) {
                    db.inventoryDao().deleteInventoryById(productId)
                    withContext(Dispatchers.Main) {
                        // TODO: redirigir a HU 3.0 (Home Inventario)
                        findNavController().popBackStack()
                    }
                }
            }.show()
    }
}
