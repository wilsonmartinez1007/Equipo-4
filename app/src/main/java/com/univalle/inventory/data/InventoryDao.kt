package com.univalle.inventory.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.univalle.inventory.model.Inventory

@Dao
interface InventoryDao {
    //funcion para insertar/guardar en la base de
    //datos nuestro inventario con los campos del producto
    //trabaja en segundo plano
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveInventory(inventory: Inventory)
    @Query("SELECT * FROM Inventory")
    suspend fun getAllInventories(): List<Inventory>
    @Query("SELECT * FROM Inventory WHERE id = :id LIMIT 1")
    suspend fun getInventoryById(id: Int): Inventory?
    @Query("DELETE FROM Inventory WHERE id = :id")
    suspend fun deleteInventoryById(id: Int)

}