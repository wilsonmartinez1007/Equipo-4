package com.univalle.inventory.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Update
import androidx.room.Query
import androidx.room.OnConflictStrategy
import com.univalle.inventory.model.Inventory

@Dao
interface InventoryDao {
    // Función para insertar/guardar en la base de datos
    // nuestro inventario con los campos del producto
    // trabaja en segundo plano
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveInventory(inventory: Inventory)

    // Obtener todos los inventarios
    @Query("SELECT * FROM inventory_table")
    suspend fun getAllInventories(): List<Inventory>

    // Obtener un inventario por ID
    @Query("SELECT * FROM inventory_table WHERE id = :id")
    suspend fun getInventoryById(id: Int): Inventory?

    // Eliminar un inventario por ID
    @Query("DELETE FROM inventory_table WHERE id = :id")
    suspend fun deleteInventoryById(id: Int)

    // Función de actualizar en la base de datos
    @Update
    suspend fun update(inventory: Inventory)

    // FUNCION TEMPORAL: Cuenta la cantidad de registros en la DB
    // Esta valida si hay registros previos para evitar duplicidad
    // de los registros de prueba que se insertan en la db
    @Query("SELECT COUNT(*) FROM inventory_table")
    suspend fun getCount(): Int

    // NUEVO: Método para la HU 1 - Widget de Inventario
    // Obtiene el valor total del inventario (suma de precio * cantidad)
    @Query("SELECT SUM(price * quantity) FROM inventory_table")
    suspend fun getTotalValue(): Double?
}