package com.univalle.inventory.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.univalle.inventory.model.Inventory

@Dao
interface InventoryDao {
    //funcion para insertar/guardar en la base de
    //datos nuestro inventario con los campos del producto
    //trabaja en segundo plano
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveInventory(inventory: Inventory)

}