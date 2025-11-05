package com.univalle.inventory.repository

import android.content.Context
import com.univalle.inventory.data.InventoryDB
import com.univalle.inventory.data.InventoryDao
import com.univalle.inventory.model.Inventory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class InventoryRepository(val context: Context) {
    private var inventoryDao: InventoryDao= InventoryDB.getDatabase(context).inventoryDao()
    suspend fun saveInventory(inventory: Inventory, messageResponse: (String) -> Unit){
        try {
            withContext(Dispatchers.IO){
                inventoryDao.saveInventory(inventory)
            }
            messageResponse("El inventario ha sido guardado con exito")
        }catch (e: Exception){
            messageResponse("Error al guardar el inventario: ${e.message}")

        }
    }
}