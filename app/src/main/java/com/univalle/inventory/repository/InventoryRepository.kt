package com.univalle.inventory.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.univalle.inventory.data.InventoryDB
import com.univalle.inventory.data.InventoryDao
import com.univalle.inventory.model.Inventory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
class InventoryRepository(private val context: Context) {

    private lateinit var db: InventoryDB
    private val inventoryDao: InventoryDao =
        InventoryDB.getDatabase(context).inventoryDao()

    // HU 4.0: guardar
    suspend fun saveInventory(inventory: Inventory, messageResponse: (String) -> Unit) {
        db = InventoryDB.getDatabase(context)

        val product = db.inventoryDao().getInventoryById(inventory.id)
        val nombre = db.inventoryDao().getName(inventory.id)
        val precio = db.inventoryDao().getPrecio(inventory.id)
        if(product!=null){
            if(nombre == inventory.name && precio == inventory.price){
                val data: String = (inventoryDao.getCantidad(inventory.id)).toString()
                val inventoryA= Inventory  (
                    id = inventory.id,
                    name = inventory.name,
                    price = inventory.price,
                    quantity = inventory.quantity + data.toInt()
                )

                try {
                    withContext(Dispatchers.IO) {
                        inventoryDao.saveInventory(inventoryA)
                    }
                    messageResponse("El inventario ha sido guardado con éxito")
                } catch (e: Exception) {
                    messageResponse("Error al guardar el inventario: ${e.message}")
                }
            }else{
                //como hago para que muestre el mensaje de abajo completo en la app
                messageResponse("Vuelve a intentarlo con los datos validos")

            }

        }else{
            try {
            withContext(Dispatchers.IO) {
                inventoryDao.saveInventory(inventory)
            }
            messageResponse("El inventario ha sido guardado con éxito")
        } catch (e: Exception) {
            messageResponse("Error al guardar el inventario: ${e.message}")
        }}

    }

    // HU 3.0: lista para el Home
    suspend fun getListInventory(): List<Inventory> =
        withContext(Dispatchers.IO) { inventoryDao.getAllInventories() }

    suspend fun getInventoryById(itemId: Int): Inventory? =
        withContext(Dispatchers.IO) { inventoryDao.getInventoryById(itemId) }
    fun observeInventories(): LiveData<List<Inventory>> =
        inventoryDao.observeInventories()
    // (Opcional) eliminar por id
    suspend fun deleteById(itemId: Int) =
        withContext(Dispatchers.IO) { inventoryDao.deleteInventoryById(itemId) }

}
