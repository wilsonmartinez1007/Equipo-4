package com.univalle.inventory.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.univalle.inventory.model.Inventory
import com.univalle.inventory.utils.Constants.NAME_BD

// ⚠️ Si CAMBIASTE el tableName de la entidad (p.ej. a "inventory_table"),
// sube la version a 2 y usa fallbackToDestructiveMigration() mientras desarrollas.
// Si NO cambiaste el esquema, puedes dejar version = 1 y quitar el fallback.
@Database(
    entities = [Inventory::class],
    version = 2,                 // ← pon 2 si cambiaste el esquema; si no, deja 1
    exportSchema = false
)
abstract class InventoryDB : RoomDatabase() {

    abstract fun inventoryDao(): InventoryDao

    companion object {
        @Volatile
        private var INSTANCE: InventoryDB? = null

        fun getDatabase(context: Context): InventoryDB =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    InventoryDB::class.java,
                    NAME_BD  // ej: "inventory_db"
                )
                    // En desarrollo facilita cuando cambias el esquema (borra y recrea la DB)
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
