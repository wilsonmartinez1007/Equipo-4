package com.univalle.inventory.view.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.univalle.inventory.R
import android.widget.TextView
import androidx.core.os.bundleOf
import com.univalle.inventory.model.Inventory
import com.univalle.inventory.viewmodel.InventoryViewModel
import java.text.NumberFormat
import java.util.*

class ProductDetailFragment : Fragment() {

    private var productId: Int = -1
    private val inventoryViewModel: InventoryViewModel by viewModels()

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

        tvName = view.findViewById(R.id.tvName)
        tvPrice = view.findViewById(R.id.tvPrice)
        tvQuantity = view.findViewById(R.id.tvQuantity)
        tvTotal = view.findViewById(R.id.tvTotal)
        btnDelete = view.findViewById(R.id.btnDelete)
        fabEdit = view.findViewById(R.id.fabEdit)
        toolbar = view.findViewById(R.id.toolbarDetail)

        arguments?.let {
            productId = it.getInt("productId", -1)
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        if (productId != -1) {
            loadProductDetail()
        }

        btnDelete.setOnClickListener {
            showDeleteDialog()
        }

        fabEdit.setOnClickListener {
            findNavController().navigate(R.id.action_productDetailFragment_to_editItemFragment, bundleOf("productId" to productId))
        }
    }

    private fun loadProductDetail() {
        inventoryViewModel.getInventoryById(productId).observe(viewLifecycleOwner) { product ->
            product?.let { bindProductData(it) }
        }
    }

    private fun bindProductData(product: Inventory) {
        val precio = NumberFormat.getNumberInstance(Locale("es", "CO"))
        precio.maximumFractionDigits = 2
        precio.minimumFractionDigits = 2

        tvName.text = product.name
        tvPrice.text = "Precio unidad: $${precio.format(product.price)}"
        tvQuantity.text = "Cantidad disponible: ${product.quantity}"
        tvTotal.text = "Total: $${precio.format(product.price * product.quantity)}"
    }

    private fun showDeleteDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Confirmar eliminación")
            .setMessage("¿Deseas eliminar este producto?")
            .setNegativeButton("No", null)
            .setPositiveButton("Sí") { _, _ ->
                deleteProduct()
            }.show()
    }

    private fun deleteProduct() {
        inventoryViewModel.deleteInventoryById(productId) {
            findNavController().popBackStack()
        }
    }
}