package com.univalle.inventory.viewholder

import android.os.Bundle
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.univalle.inventory.R
import com.univalle.inventory.databinding.ItemProductoBinding
import com.univalle.inventory.model.Inventory
import java.text.NumberFormat
import java.util.Locale

class InventoryViewHolder(
    private val binding: ItemProductoBinding,
    private val navController: NavController
) : RecyclerView.ViewHolder(binding.root) {

    fun setItemInventory(item: Inventory) {
        binding.tvNombre.text = item.name
        binding.tvId.text = "Id: ${item.id}"
        binding.tvPrecio.text = formatCOP(item.price)

        binding.root.setOnClickListener {
            val args = Bundle().apply { putInt("productId", item.id) }
            navController.navigate(
                R.id.action_homeInventoryFragment_to_productDetailFragment,
                args
            )
        }
    }

    // price Int -> "$ 23.000,00"
    private fun formatCOP(value: Int): String {
        val nf = NumberFormat.getNumberInstance(Locale("es", "CO")).apply {
            minimumFractionDigits = 0
            maximumFractionDigits = 0
        }
        return "$ ${nf.format(value)},00"
    }
}
