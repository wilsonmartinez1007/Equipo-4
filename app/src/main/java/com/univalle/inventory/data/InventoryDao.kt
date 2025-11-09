package com.univalle.inventory.data
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.univalle.inventory.model.Inventory

@Dao
interface InventoryDao {


    // Insertar / guardar inventario
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveInventory(inventory: Inventory)

    // Obtener todos los inventarios
    @Query("SELECT * FROM inventory_table")
    suspend fun getAllInventories(): List<Inventory>

    // Obtener un inventario por ID
    @Query("SELECT * FROM inventory_table WHERE id = :id LIMIT 1")
    suspend fun getInventoryById(id: Int): Inventory?

    // Eliminar un inventario por ID
    @Query("DELETE FROM inventory_table WHERE id = :id")
    suspend fun deleteInventoryById(id: Int)

    // Actualizar un inventario
    @Update
    suspend fun update(inventory: Inventory)

    // Contar registros (útil para sembrado de datos de prueba)
    @Query("SELECT COUNT(*) FROM inventory_table")
    suspend fun getCount(): Int

    // Valor total del inventario (precio * cantidad)
    @Query("SELECT SUM(price * quantity) FROM inventory_table")
    suspend fun getTotalValue(): Double?
    @Query("SELECT * FROM inventory_table ORDER BY id DESC")
    fun observeInventories(): LiveData<List<Inventory>>



}
