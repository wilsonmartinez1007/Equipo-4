package com.univalle.inventory.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Update
import androidx.room.Query
import androidx.room.OnConflictStrategy
import com.univalle.inventory.model.Inventory
import androidx.lifecycle.LiveData

@Dao
interface InventoryDao {
    //funcion para insertar/guardar en la base de
    //datos nuestro inventario con los campos del producto
    //trabaja en segundo plano
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveInventory(inventory: Inventory)

    // Funcion de actualizacr en la base de datos
    @Update
    suspend fun update(inventory: Inventory)

    // Funcion para obtener un registro por id
    @Query("SELECT * FROM inventory_table WHERE id = :itemId")
    suspend fun getInventoryById(itemId: Int): Inventory?

    // FUNCION TEMPORAL Cuenta la cantidad de registros en la DB
    // Esta valida si hay registros previos para evitar duplicidad
    // de los registros de prueba que se insertan en la db
    @Query("SELECT COUNT(*) FROM inventory_table")
    suspend fun getCount(): Int
}