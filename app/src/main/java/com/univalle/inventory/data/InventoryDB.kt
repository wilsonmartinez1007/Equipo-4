package com.univalle.inventory.data

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Database
import com.univalle.inventory.model.Inventory
import com.univalle.inventory.utils.Constants.NAME_BD

@Database(entities = [Inventory::class], version= 1)
abstract class InventoryDB : RoomDatabase() {
    abstract fun inventoryDao(): InventoryDao
    companion object{
        //creamos la base de datos con el nombre de : NAME_BD el cual se asigna en: Constants
        fun getDatabase(context: Context): InventoryDB{
            return  Room.databaseBuilder(
                context.applicationContext,
                InventoryDB::class.java,
                    NAME_BD
            ).build()

        }
    }
}