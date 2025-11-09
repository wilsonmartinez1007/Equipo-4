package com.univalle.inventory.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.univalle.inventory.data.InventoryDB
import com.univalle.inventory.data.InventoryDao
import com.univalle.inventory.model.Inventory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class InventoryRepository(context: Context) {

    private val inventoryDao: InventoryDao =
        InventoryDB.getDatabase(context).inventoryDao()

    // HU 4.0: guardar
    suspend fun saveInventory(inventory: Inventory, messageResponse: (String) -> Unit) {
        try {
            withContext(Dispatchers.IO) {
                inventoryDao.saveInventory(inventory)
            }
            messageResponse("El inventario ha sido guardado con éxito")
        } catch (e: Exception) {
            messageResponse("Error al guardar el inventario: ${e.message}")
        }
    }

    // HU 3.0: lista para el Home
    suspend fun getListInventory(): List<Inventory> =
        withContext(Dispatchers.IO) { inventoryDao.getAllInventories() }

    // (Opcional) eliminar por id
    suspend fun deleteById(itemId: Int) =
        withContext(Dispatchers.IO) { inventoryDao.deleteInventoryById(itemId) }

}
