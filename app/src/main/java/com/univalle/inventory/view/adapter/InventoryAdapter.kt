package com.univalle.inventory.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.univalle.inventory.databinding.ItemProductoBinding
import com.univalle.inventory.model.Inventory
import com.univalle.inventory.viewholder.InventoryViewHolder

class InventoryAdapter(
    private val items: MutableList<Inventory>,
    private val navController: NavController
) : RecyclerView.Adapter<InventoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventoryViewHolder {
        val binding = ItemProductoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return InventoryViewHolder(binding, navController)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: InventoryViewHolder, position: Int) {
        holder.setItemInventory(items[position])
    }

    fun replaceAll(newList: List<Inventory>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }
}
